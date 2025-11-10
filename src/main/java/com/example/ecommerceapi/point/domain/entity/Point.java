package com.example.ecommerceapi.point.domain.entity;

import com.example.ecommerceapi.common.exception.ErrorCode;
import com.example.ecommerceapi.common.exception.PointException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Point {
    private Integer pointId;
    private Integer userId;
    private PointType pointType;
    private Integer pointAmount;
    private LocalDateTime createdAt;

    /**
     * 포인트 충전 이력을 생성합니다.
     * @param userId 사용자 ID
     * @param amount 충전 금액
     * @return 충전 이력 Point 객체
     */
    public static Point createChargeHistory(Integer userId, Integer amount) {
        return Point.builder()
                .userId(userId)
                .pointType(PointType.CHARGE)
                .pointAmount(amount)
                .createdAt(LocalDateTime.now())
                .build();
    }

    /**
     * 포인트 사용 이력을 생성합니다.
     * @param userId 사용자 ID
     * @param amount 사용 금액
     * @return 사용 이력 Point 객체
     */
    public static Point createUseHistory(Integer userId, Integer amount) {
        return Point.builder()
                .userId(userId)
                .pointType(PointType.USE)
                .pointAmount(amount)
                .createdAt(LocalDateTime.now())
                .build();
    }

    /**
     * 포인트 환불 이력을 생성합니다.
     * @param userId 사용자 ID
     * @param amount 환불 금액
     * @return 환불 이력 Point 객체
     */
    public static Point createRefundHistory(Integer userId, Integer amount) {
        return Point.builder()
                .userId(userId)
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