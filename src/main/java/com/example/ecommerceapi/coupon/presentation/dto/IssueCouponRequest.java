package com.example.ecommerceapi.coupon.presentation.dto;

import com.example.ecommerceapi.coupon.application.dto.IssueCouponCommand;
import jakarta.validation.constraints.NotNull;

public record IssueCouponRequest(
        @NotNull(message = "userId는 필수입니다.")
        Integer userId,

        @NotNull(message = "couponId는 필수입니다.")
        Integer couponId
) {
    public IssueCouponCommand toCommand() {
        return new IssueCouponCommand(
                this.userId,
                this.couponId
        );
    }
}