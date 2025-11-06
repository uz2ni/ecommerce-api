package com.example.ecommerceapi.order.application.dto;

import com.example.ecommerceapi.order.domain.entity.Order;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResult {
    private Integer orderId;
    private String orderStatus;
    private Integer paymentAmount;
    private Integer remainingPoint;

    public static PaymentResult from(Order order, Integer remainingPoint) {
        return PaymentResult.builder()
                .orderId(order.getOrderId())
                .orderStatus(order.getOrderStatus().name())
                .paymentAmount(order.getFinalPaymentAmount())
                .remainingPoint(remainingPoint)
                .build();
    }
}