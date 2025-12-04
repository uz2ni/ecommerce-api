package com.example.ecommerceapi.order.domain.event;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 주문 결제 완료 이벤트
 * 결제가 완료되면 발행되어 판매 랭킹 등 부가 기능을 트리거합니다.
 */
public record OrderPaidEvent(
        Integer orderId,
        Integer userId,
        List<OrderItemDto> orderItems,
        LocalDateTime paidAt
) {
    /**
     * 주문 상품 정보
     */
    public record OrderItemDto(
            Integer productId,
            Integer orderQuantity
    ) {}
}