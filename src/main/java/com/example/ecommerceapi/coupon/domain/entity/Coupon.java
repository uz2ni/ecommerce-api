package com.example.ecommerceapi.coupon.domain.entity;

import com.example.ecommerceapi.common.exception.CouponException;
import com.example.ecommerceapi.common.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "coupon", indexes = {
    @Index(name = "idx_coupon_expired_at", columnList = "expired_at")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Coupon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "coupon_id")
    private Integer couponId;

    @Column(name = "coupon_name", nullable = false, length = 100)
    private String couponName;

    @Column(name = "discount_amount", nullable = false)
    private Integer discountAmount;

    @Column(name = "total_quantity", nullable = false)
    private Integer totalQuantity;

    @Column(name = "issued_quantity", nullable = false)
    private Integer issuedQuantity;

    @Column(name = "expired_at")
    private LocalDateTime expiredAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Version
    @Column(nullable = false)
    private Integer version;

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
