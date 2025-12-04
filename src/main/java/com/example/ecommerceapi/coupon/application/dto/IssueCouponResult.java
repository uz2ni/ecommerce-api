package com.example.ecommerceapi.coupon.application.dto;

import com.example.ecommerceapi.coupon.domain.entity.CouponUser;

public record IssueCouponResult(
        Integer couponUserId,
        Integer couponId,
        Integer userId,
        String eventId,
        String status
) {
    public static IssueCouponResult from(CouponUser couponUser) {
        return new IssueCouponResult(
                couponUser.getCouponUserId(),
                couponUser.getCoupon().getCouponId(),
                couponUser.getUser().getUserId(),
                null,
                "ISSUED"
        );
    }

    /**
     * 쿠폰 발급 요청이 접수되었을 때 (비동기 처리 대기 중)
     */
    public static IssueCouponResult pending(Integer couponId, Integer userId, String eventId) {
        return new IssueCouponResult(
                null,
                couponId,
                userId,
                eventId,
                "PENDING"
        );
    }
}
