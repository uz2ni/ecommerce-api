package com.example.ecommerceapi.coupon.infrastructure.stream.config;

/**
 * 쿠폰 발급 관련 Redis Stream 상수
 */
public final class CouponStreamConstants {

    private CouponStreamConstants() {
        throw new AssertionError("Cannot instantiate constants class");
    }

    public static final String COUPON_STREAM_KEY = "stream:coupon:issue";
    public static final String COUPON_CONSUMER_GROUP = "coupon-issue-group";
    public static final String COUPON_CONSUMER_NAME = "coupon-issue-consumer";
}