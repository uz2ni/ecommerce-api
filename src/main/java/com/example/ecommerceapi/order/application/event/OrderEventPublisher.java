package com.example.ecommerceapi.order.application.event;

import com.example.ecommerceapi.order.domain.entity.Order;
import com.example.ecommerceapi.order.domain.entity.OrderItem;
import com.example.ecommerceapi.order.domain.event.OrderPaidEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 주문 관련 도메인 이벤트 발행을 담당하는 클래스
 * OrderService의 이벤트 발행 책임을 분리하여 관심사를 명확히 합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventPublisher {
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 주문 결제 완료 이벤트 발행
     * 트랜잭션 커밋 후 비동기로 처리되어 판매 랭킹 업데이트 등 부가 기능을 수행합니다.
     *
     * @param order 결제 완료된 주문
     * @param orderItems 주문 항목 리스트
     */
    public void publishOrderPaidEvent(Order order, List<OrderItem> orderItems) {
        try {
            List<OrderPaidEvent.OrderItemDto> itemDtos = orderItems.stream()
                    .map(item -> new OrderPaidEvent.OrderItemDto(
                            item.getProduct().getProductId(),
                            item.getOrderQuantity()
                    ))
                    .toList();

            // updatedAt을 사용하여 결제 완료 시각을 반영 (실제 결제 완료 시점)
            OrderPaidEvent event = new OrderPaidEvent(
                    order.getOrderId(),
                    order.getUser().getUserId(),
                    itemDtos,
                    order.getUpdatedAt()
            );

            eventPublisher.publishEvent(event);
            log.info("Published OrderPaidEvent for orderId: {}", order.getOrderId());
        } catch (Exception e) {
            // 이벤트 발행 실패가 메인 결제 로직을 방해하지 않도록 예외를 삼킴
            log.error("Failed to publish OrderPaidEvent for orderId: {}",
                     order.getOrderId(), e);
        }
    }
}