package com.example.ecommerceapi.coupon.domain.entity;

import com.example.ecommerceapi.common.exception.CouponException;
import com.example.ecommerceapi.common.exception.ErrorCode;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
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

    /**
     * 쿠폰이 사용 가능한 상태인지 검증합니다.
     * @throws CouponException 이미 사용된 쿠폰인 경우
     */
    public void validateUsable() {
        if (Boolean.TRUE.equals(this.used)) {
            throw new CouponException(ErrorCode.COUPON_ALREADY_USED);
        }
    }

    public void markAsUsed() {
        this.used = true;
        this.usedAt = LocalDateTime.now();
    }

    public void markAsUnused() {
        this.used = false;
        this.usedAt = null;
    }
}