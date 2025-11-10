package com.example.ecommerceapi.order.application.dto;

import com.example.ecommerceapi.order.domain.entity.Order;

public record PaymentResult(
        Integer orderId,
        String orderStatus,
        Integer paymentAmount,
        Integer remainingPoint
) {
    public static PaymentResult from(Order order, Integer remainingPoint) {
        return new PaymentResult(
                order.getOrderId(),
                order.getOrderStatus().name(),
                order.getFinalPaymentAmount(),
                remainingPoint
        );
    }
}