package com.example.ecommerceapi.order.infrastructure.kafka.producer;

import com.example.ecommerceapi.common.infrastructure.kafka.dto.Ticket;
import com.example.ecommerceapi.order.infrastructure.kafka.dto.OrderKafkaMessage;
import com.example.ecommerceapi.order.application.publisher.OrderMessagePublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * 주문 메시지를 Kafka로 발행하는 Producer
 * OrderMessagePublisher 인터페이스의 Kafka 구현체입니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderKafkaProducer implements OrderMessagePublisher {

    private final KafkaTemplate<String, Ticket<OrderKafkaMessage>> kafkaTemplate;

    @Value("${kafka.topic.order-paid}")
    private String orderPaidTopic;

    /**
     * 주문 결제 완료 메시지를 Kafka로 발행
     *
     * @param message 주문 결제 완료 메시지
     */
    @Override
    public void publishOrderPaidMessage(OrderKafkaMessage message) {
        String key = String.valueOf(message.getOrderId());

        // Ticket으로 메시지 래핑
        Ticket<OrderKafkaMessage> ticket = new Ticket<>(orderPaidTopic, message);

        log.info("Sending order paid message to Kafka - topic: {}, orderId: {}, messageId: {}",
                orderPaidTopic, message.getOrderId(), ticket.getMessageId());

        kafkaTemplate.send(orderPaidTopic, key, ticket)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Message sent successfully - topic: {}, partition: {}, offset: {}, orderId: {}, messageId: {}",
                                result.getRecordMetadata().topic(),
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset(),
                                message.getOrderId(),
                                ticket.getMessageId());
                    } else {
                        log.error("Failed to send message to Kafka - orderId: {}, messageId: {}, error: {}",
                                message.getOrderId(), ticket.getMessageId(), ex.getMessage(), ex);
                    }
                });
    }
}
