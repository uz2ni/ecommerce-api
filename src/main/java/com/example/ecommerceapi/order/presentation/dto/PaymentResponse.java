package com.example.ecommerceapi.order.presentation.dto;

import com.example.ecommerceapi.order.application.dto.PaymentResult;

public record PaymentResponse(
        Integer orderId,
        String orderStatus,
        Integer paymentAmount,
        Integer remainingPoint
) {
    public static PaymentResponse from(PaymentResult payment) {
        return new PaymentResponse(
                payment.orderId(),
                payment.orderStatus(),
                payment.paymentAmount(),
                payment.remainingPoint()
        );
    }
}