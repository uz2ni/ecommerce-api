package com.example.ecommerceapi.cart.presentation.dto;

import com.example.ecommerceapi.cart.application.dto.CartItemResult;

import java.time.LocalDateTime;
import java.util.List;

public record CartItemResponse(
        Integer cartItemId,
        Integer userId,
        Integer productId,
        String productName,
        Integer productPrice,
        Integer quantity,
        Integer totalPrice,
        LocalDateTime createdAt
) {
    public static CartItemResponse from(CartItemResult cartItem) {
        return new CartItemResponse(
                cartItem.cartItemId(),
                cartItem.userId(),
                cartItem.productId(),
                cartItem.productName(),
                cartItem.productPrice(),
                cartItem.quantity(),
                cartItem.totalPrice(),
                cartItem.createdAt()
        );
    }

    public static List<CartItemResponse> fromList(List<CartItemResult> cartItems) {
        return cartItems.stream()
                .map(CartItemResponse::from)
                .toList();
    }
}