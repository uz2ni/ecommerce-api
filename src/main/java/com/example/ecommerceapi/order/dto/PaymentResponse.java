package com.example.ecommerceapi.order.dto;

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
    private Integer paymentAmount;
    private Integer remainingPoint;
    private String message;
}