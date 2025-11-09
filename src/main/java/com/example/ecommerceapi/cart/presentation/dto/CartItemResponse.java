package com.example.ecommerceapi.cart.presentation.dto;

import com.example.ecommerceapi.cart.application.dto.CartItemResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemResponse {
    private Integer cartItemId;
    private Integer userId;
    private Integer productId;
    private String productName;
    private Integer productPrice;
    private Integer quantity;
    private Integer totalPrice;
    private LocalDateTime createdAt;

    public static CartItemResponse from(CartItemResult cartItem) {
        return CartItemResponse.builder()
                .cartItemId(cartItem.getCartItemId())
                .userId(cartItem.getUserId())
                .productId(cartItem.getProductId())
                .productName(cartItem.getProductName())
                .productPrice(cartItem.getProductPrice())
                .quantity(cartItem.getQuantity())
                .totalPrice(cartItem.getTotalPrice())
                .createdAt(cartItem.getCreatedAt())
                .build();
    }

    public static List<CartItemResponse> fromList(List<CartItemResult> cartItems) {
        return cartItems.stream()
                .map(CartItemResponse::from)
                .toList();
    }
}