package com.example.ecommerceapi.order.application.dto;

public record CreateOrderCommand(
        Integer userId,
        String deliveryUsername,
        String deliveryAddress,
        Integer couponId
) {
}