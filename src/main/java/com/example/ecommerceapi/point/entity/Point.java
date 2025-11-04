package com.example.ecommerceapi.point.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
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
}