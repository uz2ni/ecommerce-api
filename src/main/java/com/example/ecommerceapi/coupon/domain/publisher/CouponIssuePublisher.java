package com.example.ecommerceapi.coupon.domain.publisher;

/**
 * 쿠폰 발급 이벤트 발행 인터페이스
 */
public interface CouponIssuePublisher {

    /**
     * 쿠폰 발급 요청 이벤트 발행
     *
     * @param couponId 쿠폰 ID
     * @param userId 사용자 ID
     * @return 이벤트 ID
     */
    String publish(Integer couponId, Integer userId);
}