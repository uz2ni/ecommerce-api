package com.example.ecommerceapi.coupon.application.dto;

import com.example.ecommerceapi.coupon.domain.entity.Coupon;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponResult {
    private Integer couponId;
    private String couponName;
    private Integer discountAmount;
    private Integer totalQuantity;
    private Integer issuedQuantity;
    private Integer remainingQuantity;
    private LocalDateTime expiredAt;
    private String status;

    public static CouponResult from(Coupon coupon) {
        return CouponResult.builder()
                .couponId(coupon.getCouponId())
                .couponName(coupon.getCouponName())
                .discountAmount(coupon.getDiscountAmount())
                .totalQuantity(coupon.getTotalQuantity())
                .issuedQuantity(coupon.getIssuedQuantity())
                .remainingQuantity(coupon.getRemainingQuantity())
                .expiredAt(coupon.getExpiredAt())
                .status(calculateStatus(coupon))
                .build();
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