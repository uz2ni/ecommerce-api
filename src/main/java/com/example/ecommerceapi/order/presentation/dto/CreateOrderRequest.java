package com.example.ecommerceapi.order.presentation.dto;

import com.example.ecommerceapi.order.application.dto.CreateOrderCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateOrderRequest(
        @NotNull(message = "userId는 필수입니다.")
        Integer userId,

        @NotBlank(message = "deliveryUsername는 빈 값일 수 없습니다.")
        String deliveryUsername,

        @NotBlank(message = "deliveryAddress는 빈 값일 수 없습니다.")
        String deliveryAddress,

        Integer couponId
) {
    public static CreateOrderCommand toCreateOrderCommand(CreateOrderRequest request) {
        return new CreateOrderCommand(
                request.userId(),
                request.deliveryUsername(),
                request.deliveryAddress(),
                request.couponId()
        );
    }
}