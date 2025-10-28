package com.example.ecommerceapi.service;

import com.example.ecommerceapi.dto.order.OrderResponse;
import com.example.ecommerceapi.mock.MockOrderData;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class OrderService {

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