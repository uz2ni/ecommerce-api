package com.example.ecommerceapi.cart.application.dto;

import com.example.ecommerceapi.cart.domain.entity.CartItem;

import java.time.LocalDateTime;
import java.util.List;

public record CartItemResult(
        Integer cartItemId,
        Integer userId,
        Integer productId,
        String productName,
        Integer productPrice,
        Integer quantity,
        Integer totalPrice,
        LocalDateTime createdAt
) {
    public static CartItemResult from(CartItem cartItem) {
        return new CartItemResult(
                cartItem.getCartItemId(),
                cartItem.getUserId(),
                cartItem.getProductId(),
                cartItem.getProductName(),
                cartItem.getProductPrice(),
                cartItem.getQuantity(),
                cartItem.getTotalPrice(),
                cartItem.getCreatedAt()
        );
    }

    public static List<CartItemResult> fromList(List<CartItem> cartItems) {
        return cartItems.stream()
                .map(CartItemResult::from)
                .toList();
    }
}