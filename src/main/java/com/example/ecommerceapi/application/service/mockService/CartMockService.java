package com.example.ecommerceapi.application.service.mockService;

import com.example.ecommerceapi.application.dto.cart.CartItemResponse;
import com.example.ecommerceapi.application.service.CartService;
import com.example.ecommerceapi.application.usecase.CartUseCase;
import com.example.ecommerceapi.infrastructure.memory.MockCartData;
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