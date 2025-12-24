package com.example.ecommerceapi.common.infrastructure.kafka.scheduler;

import com.example.ecommerceapi.common.infrastructure.kafka.service.KafkaFallbackService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Kafka Fallback 메시지 재발행 스케줄러
 * 주기적으로 실패한 메시지를 조회하여 재발행을 시도합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaFallbackScheduler {

    private final KafkaFallbackService kafkaFallbackService;

    /**
     * 1분마다 대기 중인 Fallback 메시지를 재발행 시도
     */
    @Scheduled(fixedDelay = 60000, initialDelay = 10000) // 1분마다, 시작 후 10초 대기
    public void retryFailedMessages() {
        try {
            log.debug("Starting fallback message retry job");
            kafkaFallbackService.retryPendingMessages();
            log.debug("Completed fallback message retry job");
        } catch (Exception e) {
            log.error("Error during fallback message retry job: {}", e.getMessage(), e);
        }
    }

    /**
     * 1시간마다 Fallback 메시지 통계 로깅
     */
    @Scheduled(cron = "0 0 * * * *") // 매 시간 정각
    public void logFallbackStats() {
        try {
            KafkaFallbackService.FallbackMessageStats stats = kafkaFallbackService.getStats();
            log.info("Fallback message stats - pending: {}, published: {}, failed: {}",
                    stats.pending(), stats.published(), stats.failed());
        } catch (Exception e) {
            log.error("Error during fallback stats logging: {}", e.getMessage(), e);
        }
    }
}
