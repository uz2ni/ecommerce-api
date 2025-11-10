package com.example.ecommerceapi.order.presentation.dto;

import com.example.ecommerceapi.order.application.dto.CreateOrderResult;

import java.time.LocalDateTime;

public record CreateOrderResponse(
        Integer orderId,
        Integer userId,
        String orderStatus,
        LocalDateTime createdAt
) {
    public static CreateOrderResponse from(CreateOrderResult order) {
        return new CreateOrderResponse(
                order.orderId(),
                order.userId(),
                order.orderStatus(),
                order.createdAt()
        );
    }
}