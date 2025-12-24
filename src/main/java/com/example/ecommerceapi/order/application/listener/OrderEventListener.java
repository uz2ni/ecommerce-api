package com.example.ecommerceapi.order.application.listener;

import com.example.ecommerceapi.order.infrastructure.kafka.dto.OrderKafkaMessage;
import com.example.ecommerceapi.order.application.publisher.OrderMessagePublisher;
import com.example.ecommerceapi.order.domain.event.OrderPaidEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 주문 도메인 이벤트를 처리하는 리스너
 * 주문 결제 완료 이벤트를 받아 외부 메시징 시스템으로 발행합니다.
 * 트랜잭션 커밋 후 비동기로 실행되어 메인 비즈니스 로직에 영향을 주지 않습니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventListener {

    private final OrderMessagePublisher orderMessagePublisher;

    /**
     * 주문 결제 완료 이벤트를 외부 메시징 시스템으로 발행
     * 트랜잭션 커밋 후 비동기로 실행됩니다.
     *
     * @param event 주문 결제 완료 이벤트
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void handleOrderPaidEvent(OrderPaidEvent event) {
        log.info("Publishing OrderPaidEvent: orderId={}", event.orderId());

        try {
            // 도메인 이벤트를 메시지로 변환
            OrderKafkaMessage message = convertToKafkaMessage(event);

            // 외부 메시징 시스템으로 발행
            orderMessagePublisher.publishOrderPaidMessage(message);

            log.info("Successfully published OrderPaidEvent: orderId={}", event.orderId());
        } catch (Exception e) {
            // 발행 실패는 메인 비즈니스 로직에 영향을 주지 않음
            log.error("Failed to publish OrderPaidEvent: orderId={}, error={}",
                    event.orderId(), e.getMessage(), e);
        }
    }

    /**
     * OrderPaidEvent를 Kafka 메시지로 변환
     */
    private OrderKafkaMessage convertToKafkaMessage(OrderPaidEvent event) {
        List<OrderKafkaMessage.OrderItemInfo> orderItems = event.orderItems().stream()
                .map(item -> OrderKafkaMessage.OrderItemInfo.builder()
                        .productId(item.productId())
                        .orderQuantity(item.orderQuantity())
                        .build())
                .collect(Collectors.toList());

        return OrderKafkaMessage.builder()
                .orderId(event.orderId())
                .userId(event.userId())
                .paidAt(event.paidAt())
                .orderItems(orderItems)
                .build();
    }
}
