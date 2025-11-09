package com.example.ecommerceapi.coupon.domain.entity;

import com.example.ecommerceapi.common.exception.CouponException;
import com.example.ecommerceapi.common.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
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

    public void issueCoupon() {
        if (!isAvailable()) {
            throw new CouponException(ErrorCode.COUPON_NOT_AVAILABLE);
        }
        this.issuedQuantity += 1;
    }
}
