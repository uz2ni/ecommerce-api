package com.example.ecommerceapi.cart.application.service;

import com.example.ecommerceapi.cart.presentation.dto.CartItemResponse;
import com.example.ecommerceapi.cart.infrastructure.MockCartData;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CartMockService {

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