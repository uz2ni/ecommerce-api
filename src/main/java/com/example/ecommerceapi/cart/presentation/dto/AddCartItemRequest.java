package com.example.ecommerceapi.cart.presentation.dto;

import com.example.ecommerceapi.cart.application.dto.AddCartItemCommand;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AddCartItemRequest(
        @NotNull(message = "userId는 필수입니다.")
        Integer userId,

        @NotNull(message = "productId는 필수입니다.")
        Integer productId,

        @NotNull(message = "quantity는 필수입니다.")
        @Min(value = 1, message = "quantity는 1 이상이어야 합니다.")
        int quantity
) {
    public AddCartItemCommand toCommand() {
        return new AddCartItemCommand(
                this.userId,
                this.productId,
                this.quantity
        );
    }
}