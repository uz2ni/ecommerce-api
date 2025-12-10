package com.example.ecommerceapi.coupon.domain.event;

/**
 * 쿠폰 발급 이벤트 소비 인터페이스
 * - 쿠폰 발급 요청 이벤트를 수신하고 처리
 */
public interface CouponIssueConsumer {

    /**
     * 쿠폰 발급 이벤트 처리
     *
     * @param couponId 쿠폰 ID
     * @param userId 사용자 ID
     */
    void consume(Integer couponId, Integer userId);
}
