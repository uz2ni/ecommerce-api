package com.example.ecommerceapi.coupon.domain.entity;

import com.example.ecommerceapi.common.exception.CouponException;
import com.example.ecommerceapi.common.exception.ErrorCode;
import com.example.ecommerceapi.user.domain.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "coupon_user", indexes = {
    @Index(name = "idx_coupon_user_coupon_id", columnList = "coupon_id"),
    @Index(name = "idx_coupon_user_user_id", columnList = "user_id"),
    @Index(name = "idx_coupon_user_used", columnList = "used")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "coupon_user_id")
    private Integer couponUserId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Coupon coupon;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private User user;

    @Column(nullable = false)
    private Boolean used;

    @Column(name = "issued_at", nullable = false)
    private LocalDateTime issuedAt;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    public static CouponUser createIssuedCouponUser(Coupon coupon, User user) {
        return CouponUser.builder()
                .coupon(coupon)
                .user(user)
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