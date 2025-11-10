package com.example.ecommerceapi.order.presentation.dto;

import com.example.ecommerceapi.order.application.dto.OrderItemResult;

public record OrderItemResponse(
        Integer orderItemId,
        Integer orderId,
        Integer productId,
        String productName,
        String description,
        Integer productPrice,
        Integer orderQuantity,
        Integer totalPrice
) {
    public static OrderItemResponse from(OrderItemResult orderItem) {
        return new OrderItemResponse(
                orderItem.orderItemId(),
                orderItem.orderId(),
                orderItem.productId(),
                orderItem.productName(),
                orderItem.description(),
                orderItem.productPrice(),
                orderItem.orderQuantity(),
                orderItem.totalPrice()
        );
    }
}