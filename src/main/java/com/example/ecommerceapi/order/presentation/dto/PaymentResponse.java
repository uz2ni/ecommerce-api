package com.example.ecommerceapi.order.presentation.dto;

import com.example.ecommerceapi.order.application.dto.PaymentResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private Integer orderId;
    private String orderStatus;
    private Integer paymentAmount;
    private Integer remainingPoint;

    public static PaymentResponse from(PaymentResult payment) {
        return PaymentResponse.builder()
                .orderId(payment.getOrderId())
                .orderStatus(payment.getOrderStatus())
                .paymentAmount(payment.getPaymentAmount())
                .remainingPoint(payment.getRemainingPoint())
                .build();
    }
}