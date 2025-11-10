package com.example.ecommerceapi.order.application.dto;

import com.example.ecommerceapi.order.domain.entity.Order;

import java.time.LocalDateTime;

public record CreateOrderResult(
        Integer orderId,
        Integer userId,
        String orderStatus,
        LocalDateTime createdAt
) {
    public static CreateOrderResult from(Order order) {
        return new CreateOrderResult(
                order.getOrderId(),
                order.getUserId(),
                order.getOrderStatus().name(),
                order.getCreatedAt()
        );
    }
}
