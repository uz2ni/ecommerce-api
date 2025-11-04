package com.example.ecommerceapi.order.service;

import com.example.ecommerceapi.order.dto.OrderResponse;
import com.example.ecommerceapi.order.usecase.OrderUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Primary
@RequiredArgsConstructor
public class OrderService implements OrderUseCase {

    @Override
    public OrderResponse createOrder(Integer userId, String deliveryUsername, String deliveryAddress, Integer couponId) {
        // TODO: Implement
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public OrderResponse getOrder(Integer orderId) {
        // TODO: Implement
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Map<String, Object> processPayment(Integer orderId, Integer userId) {
        // TODO: Implement
        throw new UnsupportedOperationException("Not implemented yet");
    }
}