package com.example.ecommerceapi.cart.application.dto;

public record AddCartItemCommand(
        Integer userId,
        Integer productId,
        int quantity
) {
}