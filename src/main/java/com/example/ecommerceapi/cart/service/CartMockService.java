package com.example.ecommerceapi.cart.service;

import com.example.ecommerceapi.cart.dto.CartItemResponse;
import com.example.ecommerceapi.cart.usecase.CartUseCase;
import com.example.ecommerceapi.cart.infrastructure.MockCartData;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CartMockService implements CartUseCase {

    public List<CartItemResponse> getCartItems(Integer userId) {
        return MockCartData.getCartItems(userId);
    }

    public CartItemResponse addCartItem(Integer userId, Integer productId, Integer quantity) {
        return MockCartData.addCartItem(userId, productId, quantity);
    }

    public boolean deleteCartItem(Integer cartItemId) {
        return MockCartData.deleteCartItem(cartItemId);
    }
}