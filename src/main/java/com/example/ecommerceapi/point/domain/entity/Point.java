package com.example.ecommerceapi.point.domain.entity;

import com.example.ecommerceapi.common.exception.ErrorCode;
import com.example.ecommerceapi.common.exception.PointException;
import com.example.ecommerceapi.user.domain.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "point")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Point {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "point_id")
    private Integer pointId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "point_type", nullable = false, length = 10)
    private PointType pointType;

    @Column(name = "point_amount", nullable = false)
    private Integer pointAmount;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * 포인트 충전 이력을 생성합니다.
     * @param user 사용자
     * @param amount 충전 금액
     * @return 충전 이력 Point 객체
     */
    public static Point createChargeHistory(User user, Integer amount) {
        return Point.builder()
                .user(user)
                .pointType(PointType.CHARGE)
                .pointAmount(amount)
                .createdAt(LocalDateTime.now())
                .build();
    }

    /**
     * 포인트 사용 이력을 생성합니다.
     * @param user 사용자
     * @param amount 사용 금액
     * @return 사용 이력 Point 객체
     */
    public static Point createUseHistory(User user, Integer amount) {
        return Point.builder()
                .user(user)
                .pointType(PointType.USE)
                .pointAmount(amount)
                .createdAt(LocalDateTime.now())
                .build();
    }

    /**
     * 포인트 환불 이력을 생성합니다.
     * @param user 사용자
     * @param amount 환불 금액
     * @return 환불 이력 Point 객체
     */
    public static Point createRefundHistory(User user, Integer amount) {
        return Point.builder()
                .user(user)
                .pointType(PointType.REFUND)
                .pointAmount(amount)
                .createdAt(LocalDateTime.now())
                .build();
    }

    /**
     * 포인트 금액이 유효한지 검증합니다.
     * @param amount 검증할 금액
     * @throws PointException 금액이 null이거나 범위를 벗어난 경우
     */
    public static void validatePointAmount(Integer amount, Integer minAmount, Integer maxAmount) {
        if ((amount == null) || (amount < minAmount) || (amount > maxAmount)) {
            throw new PointException(ErrorCode.POINT_INVALID_AMOUNT);
        }
    }
}