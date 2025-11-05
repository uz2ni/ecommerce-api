package com.example.ecommerceapi.coupon.presentation.dto;

import com.example.ecommerceapi.coupon.application.dto.IssueCouponResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IssueCouponResponse {
    private Integer couponUserId;
    private Integer couponId;
    private Integer userId;

    public static IssueCouponResponse from(IssueCouponResult result) {
        return IssueCouponResponse.builder()
                .couponUserId(result.getCouponUserId())
                .couponId(result.getCouponId())
                .userId(result.getUserId())
                .build();
    }
}