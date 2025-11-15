package com.example.ecommerceapi.order.application.dto;

import com.example.ecommerceapi.order.domain.entity.OrderItem;

public record OrderItemResult(
        Integer orderItemId,
        Integer orderId,
        Integer productId,
        String productName,
        String description,
        Integer productPrice,
        Integer orderQuantity,
        Integer totalPrice
) {
    public static OrderItemResult from(OrderItem orderItem) {
        return new OrderItemResult(
                orderItem.getOrderItemId(),
                orderItem.getOrder().getOrderId(),
                orderItem.getProduct().getProductId(),
                orderItem.getProductName(),
                orderItem.getDescription(),
                orderItem.getProductPrice(),
                orderItem.getOrderQuantity(),
                orderItem.getTotalPrice()
        );
    }
}
