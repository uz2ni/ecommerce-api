package com.example.ecommerceapi.coupon.application.dto;

import com.example.ecommerceapi.coupon.domain.entity.Coupon;
import com.example.ecommerceapi.coupon.domain.entity.CouponUser;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IssueCouponResult {
    private Integer couponUserId;
    private Integer couponId;
    private Integer userId;

    public static IssueCouponResult from(CouponUser couponUser) {
        return IssueCouponResult.builder()
                .couponUserId(couponUser.getCouponUserId())
                .couponId(couponUser.getCouponId())
                .userId(couponUser.getUserId())
                .build();
    }
}
