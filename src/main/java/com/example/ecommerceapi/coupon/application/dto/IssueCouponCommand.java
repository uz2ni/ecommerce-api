package com.example.ecommerceapi.coupon.application.dto;

public record IssueCouponCommand(
        Integer userId,
        Integer couponId
) {
}