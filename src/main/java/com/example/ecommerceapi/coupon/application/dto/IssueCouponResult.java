package com.example.ecommerceapi.coupon.application.dto;

import com.example.ecommerceapi.coupon.domain.entity.CouponUser;

public record IssueCouponResult(
        Integer couponUserId,
        Integer couponId,
        Integer userId
) {
    public static IssueCouponResult from(CouponUser couponUser) {
        return new IssueCouponResult(
                couponUser.getCouponUserId(),
                couponUser.getCoupon().getCouponId(),
                couponUser.getUser().getUserId()
        );
    }
}
