package com.example.ecommerceapi.application.service.mockService;

import com.example.ecommerceapi.application.dto.order.OrderResponse;
import com.example.ecommerceapi.application.usecase.CartUseCase;
import com.example.ecommerceapi.application.usecase.OrderUseCase;
import com.example.ecommerceapi.infrastructure.memory.MockOrderData;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class OrderMockService implements OrderUseCase {

    public OrderResponse createOrder(Integer userId, String deliveryUsername, String deliveryAddress, Integer couponId) {
        return MockOrderData.createOrder(userId, deliveryUsername, deliveryAddress, couponId);
    }

    public OrderResponse getOrder(Integer orderId) {
        return MockOrderData.getOrder(orderId);
    }

    public Map<String, Object> processPayment(Integer orderId, Integer userId) {
        return MockOrderData.processPayment(orderId, userId);
    }
}