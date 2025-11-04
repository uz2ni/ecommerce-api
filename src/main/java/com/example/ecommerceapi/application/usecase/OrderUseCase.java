package com.example.ecommerceapi.application.usecase;

import com.example.ecommerceapi.application.dto.order.OrderResponse;

import java.util.Map;

public interface OrderUseCase {

    OrderResponse createOrder(Integer userId, String deliveryUsername, String deliveryAddress, Integer couponId);

    OrderResponse getOrder(Integer orderId);

    Map<String, Object> processPayment(Integer orderId, Integer userId);
}