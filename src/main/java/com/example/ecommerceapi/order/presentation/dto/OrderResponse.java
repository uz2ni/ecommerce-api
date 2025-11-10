package com.example.ecommerceapi.order.presentation.dto;

import com.example.ecommerceapi.order.application.dto.OrderResult;

import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Integer orderId,
        Integer userId,
        String orderStatus,
        Integer totalOrderAmount,
        Integer totalDiscountAmount,
        Integer usedPoint,
        Integer finalPaymentAmount,
        String deliveryUsername,
        String deliveryAddress,
        Integer couponId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<OrderItemResponse> orderItems
) {
    public static OrderResponse buildGetOrder(OrderResult order) {
        List<OrderItemResponse> orderItems = order.orderItems().stream().map(OrderItemResponse::from).toList();

        return new OrderResponse(
                order.orderId(),
                order.userId(),
                order.orderStatus(),
                order.totalOrderAmount(),
                order.totalDiscountAmount(),
                order.usedPoint(),
                order.finalPaymentAmount(),
                order.deliveryUsername(),
                order.deliveryAddress(),
                order.couponId(),
                order.createdAt(),
                order.updatedAt(),
                orderItems
        );
    }
}