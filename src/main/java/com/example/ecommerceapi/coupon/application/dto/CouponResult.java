package com.example.ecommerceapi.coupon.application.dto;

import com.example.ecommerceapi.coupon.domain.entity.Coupon;

import java.time.LocalDateTime;
import java.util.List;

public record CouponResult(
        Integer couponId,
        String couponName,
        Integer discountAmount,
        Integer totalQuantity,
        Integer issuedQuantity,
        Integer remainingQuantity,
        LocalDateTime expiredAt,
        String status
) {
    public static CouponResult from(Coupon coupon) {
        return new CouponResult(
                coupon.getCouponId(),
                coupon.getCouponName(),
                coupon.getDiscountAmount(),
                coupon.getTotalQuantity(),
                coupon.getIssuedQuantity(),
                coupon.getRemainingQuantity(),
                coupon.getExpiredAt(),
                calculateStatus(coupon)
        );
    }

    public static List<CouponResult> fromList(List<Coupon> coupons) {
        return coupons.stream()
                .map(CouponResult::from)
                .toList();
    }

    private static String calculateStatus(Coupon coupon) {
        if (coupon.getRemainingQuantity() <= 0) {
            return "DEPLETED";
        }
        if (coupon.isExpired()) {
            return "EXPIRED";
        }
        return "ACTIVE";
    }
}