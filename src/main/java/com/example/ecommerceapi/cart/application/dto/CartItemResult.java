package com.example.ecommerceapi.cart.application.dto;

import com.example.ecommerceapi.cart.domain.entity.CartItem;
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
public class CartItemResult {
    private Integer cartItemId;
    private Integer userId;
    private Integer productId;
    private String productName;
    private Integer productPrice;
    private Integer quantity;
    private Integer totalPrice;
    private LocalDateTime createdAt;

    public static CartItemResult from(CartItem cartItem) {
        return CartItemResult.builder()
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

    public static List<CartItemResult> fromList(List<CartItem> cartItems) {
        return cartItems.stream()
                .map(CartItemResult::from)
                .toList();
    }
}