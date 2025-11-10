package com.example.ecommerceapi.order.presentation.dto;

import jakarta.validation.constraints.NotNull;

public record PaymentRequest(
        @NotNull(message = "orderId는 필수입니다.")
        Integer orderId,

        @NotNull(message = "userId는 필수입니다.")
        Integer userId
) {
}