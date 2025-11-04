package com.example.ecommerceapi.application.usecase;

import com.example.ecommerceapi.application.dto.cart.CartItemResponse;

import java.util.List;

public interface CartUseCase {

    List<CartItemResponse> getCartItems(Integer userId);

    CartItemResponse addCartItem(Integer userId, Integer productId, Integer quantity);

    boolean deleteCartItem(Integer cartItemId);
}