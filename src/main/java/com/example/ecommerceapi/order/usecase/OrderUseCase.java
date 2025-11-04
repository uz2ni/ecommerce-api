package com.example.ecommerceapi.order.usecase;

import com.example.ecommerceapi.order.dto.OrderResponse;

import java.util.Map;

public interface OrderUseCase {

    OrderResponse createOrder(Integer userId, String deliveryUsername, String deliveryAddress, Integer couponId);

    OrderResponse getOrder(Integer orderId);

    Map<String, Object> processPayment(Integer orderId, Integer userId);
}