package com.example.ecommerceapi.common.infrastructure.kafka.service;

import com.example.ecommerceapi.common.infrastructure.kafka.dto.Ticket;
import com.example.ecommerceapi.common.infrastructure.kafka.entity.KafkaFallbackMessage;
import com.example.ecommerceapi.common.infrastructure.kafka.entity.KafkaFallbackMessage.FallbackMessageStatus;
import com.example.ecommerceapi.common.infrastructure.kafka.repository.KafkaFallbackMessageRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Kafka Fallback 메시지 처리 서비스
 * Kafka 발행 실패 시 DB에 저장하고, 주기적으로 재발행을 시도합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaFallbackService {

    private final KafkaFallbackMessageRepository fallbackMessageRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Fallback 메시지 저장
     *
     * @param topic Kafka 토픽
     * @param key 메시지 키
     * @param payload 메시지 페이로드
     * @param errorMessage 에러 메시지
     */
    @Transactional
    public void saveFallbackMessage(String topic, String key, Object payload, String errorMessage) {
        try {
            String payloadJson = objectMapper.writeValueAsString(payload);

            KafkaFallbackMessage fallbackMessage = KafkaFallbackMessage.builder()
                    .topic(topic)
                    .messageKey(key)
                    .payload(payloadJson)
                    .retryCount(0)
                    .maxRetry(3)
                    .status(FallbackMessageStatus.PENDING)
                    .errorMessage(errorMessage)
                    .nextRetryAt(LocalDateTime.now().plusMinutes(1)) // 1분 후 첫 재시도
                    .build();

            fallbackMessageRepository.save(fallbackMessage);

            log.info("Saved fallback message - topic: {}, key: {}, id: {}",
                    topic, key, fallbackMessage.getId());

        } catch (Exception e) {
            log.error("Failed to save fallback message - topic: {}, key: {}, error: {}",
                    topic, key, e.getMessage(), e);
        }
    }

    /**
     * 재시도 대상 메시지 조회 및 재발행
     * 스케줄러에서 주기적으로 호출됩니다.
     */
    @Transactional
    public void retryPendingMessages() {
        List<KafkaFallbackMessage> messages = fallbackMessageRepository.findRetryableMessages(
                FallbackMessageStatus.PENDING,
                LocalDateTime.now()
        );

        log.info("Found {} pending fallback messages to retry", messages.size());

        for (KafkaFallbackMessage message : messages) {
            retryMessage(message);
        }
    }

    /**
     * 개별 메시지 재발행 시도
     *
     * @param message Fallback 메시지
     */
    private void retryMessage(KafkaFallbackMessage message) {
        try {
            log.info("Retrying fallback message - id: {}, topic: {}, retryCount: {}",
                    message.getId(), message.getTopic(), message.getRetryCount());

            // JSON을 Object로 변환 (Ticket으로 역직렬화)
            Object payload = objectMapper.readValue(message.getPayload(), Object.class);

            // Kafka로 발행
            kafkaTemplate.send(message.getTopic(), message.getMessageKey(), payload)
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            // 발행 성공
                            handleRetrySuccess(message);
                        } else {
                            // 발행 실패
                            handleRetryFailure(message, ex.getMessage());
                        }
                    });

        } catch (Exception e) {
            log.error("Failed to retry fallback message - id: {}, error: {}",
                    message.getId(), e.getMessage(), e);
            handleRetryFailure(message, e.getMessage());
        }
    }

    /**
     * 재발행 성공 처리
     *
     * @param message Fallback 메시지
     */
    @Transactional
    public void handleRetrySuccess(KafkaFallbackMessage message) {
        message.markAsPublished();
        fallbackMessageRepository.save(message);

        log.info("Fallback message published successfully - id: {}, topic: {}",
                message.getId(), message.getTopic());
    }

    /**
     * 재발행 실패 처리
     *
     * @param message Fallback 메시지
     * @param errorMessage 에러 메시지
     */
    @Transactional
    public void handleRetryFailure(KafkaFallbackMessage message, String errorMessage) {
        message.incrementRetry();
        message.setErrorMessage(errorMessage);
        fallbackMessageRepository.save(message);

        if (message.getStatus() == FallbackMessageStatus.FAILED) {
            log.error("Fallback message failed after {} retries - id: {}, topic: {}",
                    message.getRetryCount(), message.getId(), message.getTopic());
        } else {
            log.warn("Fallback message retry failed - id: {}, retryCount: {}, nextRetry: {}",
                    message.getId(), message.getRetryCount(), message.getNextRetryAt());
        }
    }

    /**
     * Fallback 메시지 통계 조회
     */
    @Transactional(readOnly = true)
    public FallbackMessageStats getStats() {
        long pendingCount = fallbackMessageRepository.countByStatus(FallbackMessageStatus.PENDING);
        long publishedCount = fallbackMessageRepository.countByStatus(FallbackMessageStatus.PUBLISHED);
        long failedCount = fallbackMessageRepository.countByStatus(FallbackMessageStatus.FAILED);

        return new FallbackMessageStats(pendingCount, publishedCount, failedCount);
    }

    /**
     * Fallback 메시지 통계
     */
    public record FallbackMessageStats(long pending, long published, long failed) {
    }
}
