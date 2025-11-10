package com.example.ecommerceapi.coupon.presentation.dto;

import com.example.ecommerceapi.coupon.application.dto.IssueCouponResult;

public record IssueCouponResponse(
        Integer couponUserId,
        Integer couponId,
        Integer userId
) {
    public static IssueCouponResponse from(IssueCouponResult result) {
        return new IssueCouponResponse(
                result.couponUserId(),
                result.couponId(),
                result.userId()
        );
    }
}