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
public class Coupon {
    private Integer couponId;
    private String couponName;
    private Integer discountAmount;
    private Integer totalQuantity;
    private Integer issuedQuantity;
    private LocalDateTime expiredAt;
    private LocalDateTime createdAt;

    public Integer getRemainingQuantity() {
        return totalQuantity - issuedQuantity;
    }

    public boolean isAvailable() {
        return getRemainingQuantity() > 0 && !isExpired();
    }

    public boolean isExpired() {
        return expiredAt != null && LocalDateTime.now().isAfter(expiredAt);
    }

    /**
     * 쿠폰이 만료되었는지 검증합니다.
     * @throws CouponException 쿠폰이 만료된 경우
     */
    public void validateNotExpired() {
        if (isExpired()) {
            throw new CouponException(ErrorCode.COUPON_EXPIRED);
        }
    }

    public void issueCoupon() {
        if (!isAvailable()) {
            throw new CouponException(ErrorCode.COUPON_NOT_AVAILABLE);
        }
        this.issuedQuantity += 1;
    }
}
