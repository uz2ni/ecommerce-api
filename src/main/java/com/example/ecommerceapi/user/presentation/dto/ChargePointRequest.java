package com.example.ecommerceapi.user.presentation.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ChargePointRequest(
        @NotNull(message = "amount는 필수입니다.")
        @Min(value = 1000, message = "amount는 1000 이상이어야 합니다.")
        Integer amount
) {
}