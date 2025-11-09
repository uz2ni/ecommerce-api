//package com.example.ecommerceapi.order.service;
//
//import com.example.ecommerceapi.order.dto.OrderResponse;
//import com.example.ecommerceapi.order.usecase.OrderUseCase;
//import com.example.ecommerceapi.order.infrastructure.MockOrderData;
//import org.springframework.stereotype.Service;
//
//import java.util.Map;
//
//@Service
//public class OrderMockService implements OrderUseCase {
//
//    public OrderResponse createOrder(Integer userId, String deliveryUsername, String deliveryAddress, Integer couponId) {
//        return MockOrderData.createOrder(userId, deliveryUsername, deliveryAddress, couponId);
//    }
//
//    public OrderResponse getOrder(Integer orderId) {
//        return MockOrderData.getOrder(orderId);
//    }
//
//    public Map<String, Object> processPayment(Integer orderId, Integer userId) {
//        return MockOrderData.processPayment(orderId, userId);
//    }
//}