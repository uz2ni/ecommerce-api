package com.example.ecommerceapi.cart.service;

import com.example.ecommerceapi.cart.dto.CartItemResponse;
import com.example.ecommerceapi.cart.usecase.CartUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Primary
@RequiredArgsConstructor
public class CartService implements CartUseCase {

    @Override
    public List<CartItemResponse> getCartItems(Integer userId) {
        // TODO: Implement
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public CartItemResponse addCartItem(Integer userId, Integer productId, Integer quantity) {
        // TODO: Implement
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public boolean deleteCartItem(Integer cartItemId) {
        // TODO: Implement
        throw new UnsupportedOperationException("Not implemented yet");
    }
}