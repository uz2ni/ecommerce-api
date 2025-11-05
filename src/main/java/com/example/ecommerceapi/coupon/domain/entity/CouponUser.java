package com.example.ecommerceapi.coupon.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponUser {
    private Integer couponUserId;
    private Integer couponId;
    private Integer userId;
    private Boolean used;
    private LocalDateTime issuedAt;
    private LocalDateTime usedAt;

    public static CouponUser createIssuedCouponUser(Integer couponId, Integer userId) {
        return CouponUser.builder()
                .couponId(couponId)
                .userId(userId)
                .used(false)
                .issuedAt(LocalDateTime.now())
                .build();
    }

    public void markAsUsed() {
        this.used = true;
        this.usedAt = LocalDateTime.now();
    }
}