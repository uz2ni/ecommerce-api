package com.example.ecommerceapi.service;

import com.example.ecommerceapi.dto.cart.CartItemResponse;
import com.example.ecommerceapi.mock.MockCartData;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CartService {

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