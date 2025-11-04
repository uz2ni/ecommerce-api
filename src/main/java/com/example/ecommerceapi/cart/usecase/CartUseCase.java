package com.example.ecommerceapi.cart.usecase;

import com.example.ecommerceapi.cart.dto.CartItemResponse;

import java.util.List;

public interface CartUseCase {

    List<CartItemResponse> getCartItems(Integer userId);

    CartItemResponse addCartItem(Integer userId, Integer productId, Integer quantity);

    boolean deleteCartItem(Integer cartItemId);
}