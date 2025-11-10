package com.example.ecommerceapi.coupon.presentation.dto;

import com.example.ecommerceapi.coupon.application.dto.CouponUserResult;

import java.time.LocalDateTime;
import java.util.List;

public record CouponUserResponse(
        Integer couponUserId,
        Integer userId,
        String userName,
        Boolean used,
        LocalDateTime issuedAt,
        LocalDateTime usedAt
) {
    public static CouponUserResponse from(CouponUserResult couponUserResult) {
        return new CouponUserResponse(
                couponUserResult.couponUserId(),
                couponUserResult.userId(),
                couponUserResult.userName(),
                couponUserResult.used(),
                couponUserResult.issuedAt(),
                couponUserResult.usedAt()
        );
    }

    public static List<CouponUserResponse> fromList(List<CouponUserResult> couponUserResults) {
        return couponUserResults.stream()
                .map(CouponUserResponse::from)
                .toList();
    }
}