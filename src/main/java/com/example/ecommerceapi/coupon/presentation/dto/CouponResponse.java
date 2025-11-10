package com.example.ecommerceapi.coupon.presentation.dto;

import com.example.ecommerceapi.coupon.application.dto.CouponResult;

import java.time.LocalDateTime;
import java.util.List;

public record CouponResponse(
        Integer couponId,
        String couponName,
        Integer discountAmount,
        Integer totalQuantity,
        Integer issuedQuantity,
        Integer remainingQuantity,
        LocalDateTime expiredAt,
        String status
) {
    public static CouponResponse from(CouponResult couponResult) {
        return new CouponResponse(
                couponResult.couponId(),
                couponResult.couponName(),
                couponResult.discountAmount(),
                couponResult.totalQuantity(),
                couponResult.issuedQuantity(),
                couponResult.remainingQuantity(),
                couponResult.expiredAt(),
                couponResult.status()
        );
    }

    public static List<CouponResponse> fromList(List<CouponResult> couponResults) {
        return couponResults.stream()
                .map(CouponResponse::from)
                .toList();
    }
}