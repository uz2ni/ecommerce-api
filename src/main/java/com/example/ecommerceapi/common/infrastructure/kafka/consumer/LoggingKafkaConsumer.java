package com.example.ecommerceapi.common.infrastructure.kafka.consumer;

import com.example.ecommerceapi.common.infrastructure.external.client.ExternalLoggingClient;
import com.example.ecommerceapi.common.infrastructure.kafka.dto.Ticket;
import com.example.ecommerceapi.order.infrastructure.kafka.dto.OrderKafkaMessage;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Kafka로부터 주문 관련 이벤트를 수신하여 외부 로깅 시스템으로 전송하는 Consumer
 * 비즈니스 로직과 분리된 인프라 계층의 로깅 처리를 담당합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LoggingKafkaConsumer {

    private final ExternalLoggingClient externalLoggingClient;

    /**
     * 주문 결제 완료 이벤트를 수신하여 외부 로깅 시스템으로 전송
     *
     * @param ticket Ticket으로 래핑된 주문 결제 완료 메시지
     * @param topic Kafka 토픽
     * @param partition 파티션 번호
     * @param offset 오프셋
     */
    @KafkaListener(
        topics = "${kafka.topic.order-paid}",
        groupId = "${kafka.consumer.group-id.logging}"
    )
    public void consumeOrderPaidEvent(
        @Payload Ticket<OrderKafkaMessage> ticket,
        @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
        @Header(KafkaHeaders.RECEIVED_PARTITION) Integer partition,
        @Header(KafkaHeaders.OFFSET) Long offset
    ) {
        // 1. LinkedHashMap을 OrderKafkaMessage로 변환하기 위한 설정
        ObjectMapper mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        try {
            // 2. convertValue를 사용하여 LinkedHashMap -> OrderKafkaMessage 강제 변환
            // 이 과정에서 LocalDateTime 배열이나 문자열도 설정에 따라 자동으로 매핑됩니다.
            OrderKafkaMessage message = mapper.convertValue(
                    ticket.getPayload(),
                    OrderKafkaMessage.class
            );

            log.info("Received order paid event from Kafka - topic: {}, partition: {}, offset: {}, orderId: {}, messageId: {}, timestamp: {}",
                    topic, partition, offset, message.getOrderId(), ticket.getMessageId(), ticket.getTimestamp());

            // 외부 로깅 시스템으로 전송
            externalLoggingClient.sendLog(message);

            log.info("Successfully forwarded order paid event to external logging system - orderId: {}, messageId: {}",
                    message.getOrderId(), ticket.getMessageId());

        } catch (IllegalArgumentException e) {
            // convertValue 실패 시 발생하는 예외 처리
            log.error("Failed to convert payload to OrderKafkaMessage. Payload: {}", ticket.getPayload(), e);
        } catch (Exception e) {
            log.error("Failed to process order paid event - orderId: {}, error: {}",
                    ticket.getMessageId(), e.getMessage(), e);
        }
    }
}
