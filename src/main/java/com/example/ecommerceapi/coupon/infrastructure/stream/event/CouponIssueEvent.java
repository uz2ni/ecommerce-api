package com.example.ecommerceapi.coupon.infrastructure.stream.event;

import java.time.LocalDateTime;

/**
 * 쿠폰 발급 이벤트
 * Redis Stream으로 전송되는 쿠폰 발급 요청 이벤트
 *
 * @param eventId 이벤트 ID (Redis Stream에서 자동 생성)
 * @param couponId 쿠폰 ID
 * @param userId 사용자 ID
 * @param createdAt 이벤트 생성 시간
 */
public record CouponIssueEvent(
        String eventId,
        Integer couponId,
        Integer userId,
        LocalDateTime createdAt
) {

    /**
     * 쿠폰 발급 요청 이벤트 생성
     */
    public static CouponIssueEvent of(Integer couponId, Integer userId) {
        return new CouponIssueEvent(
                null,
                couponId,
                userId,
                LocalDateTime.now()
        );
    }
}
