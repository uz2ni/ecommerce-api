package com.example.ecommerceapi.coupon.presentation.dto;

import com.example.ecommerceapi.coupon.application.dto.CouponResult;
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
public class CouponResponse {
    private Integer couponId;
    private String couponName;
    private Integer discountAmount;
    private Integer totalQuantity;
    private Integer issuedQuantity;
    private Integer remainingQuantity;
    private LocalDateTime expiredAt;
    private String status;

    public static CouponResponse from(CouponResult couponResult) {
        return CouponResponse.builder()
                .couponId(couponResult.getCouponId())
                .couponName(couponResult.getCouponName())
                .discountAmount(couponResult.getDiscountAmount())
                .totalQuantity(couponResult.getTotalQuantity())
                .issuedQuantity(couponResult.getIssuedQuantity())
                .remainingQuantity(couponResult.getRemainingQuantity())
                .expiredAt(couponResult.getExpiredAt())
                .status(couponResult.getStatus())
                .build();
    }

    public static List<CouponResponse> fromList(List<CouponResult> couponResults) {
        return couponResults.stream()
                .map(CouponResponse::from)
                .toList();
    }
}