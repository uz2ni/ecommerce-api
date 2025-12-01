package com.example.ecommerceapi.common.config;

import lombok.Getter;

import java.time.Duration;

/**
 * 캐시 타입 정의 Enum
 * 각 캐시의 이름과 TTL(Time To Live)을 중앙에서 관리합니다.
 */
@Getter
public enum CacheType {

    // Product 관련 캐시
    ALL_PRODUCTS("allProducts", Duration.ofMinutes(30)),
    PRODUCT("product", Duration.ofMinutes(30)),
    POPULAR_PRODUCTS_SALES("popularProducts:SALES", Duration.ofMinutes(5)),
    POPULAR_PRODUCTS_VIEWS("popularProducts:VIEWS", Duration.ofMinutes(3)),

    // Order 관련 캐시
    ORDER("order", Duration.ofMinutes(60)),

    // Coupon 관련 캐시
    ALL_COUPONS("allCoupons", Duration.ofMinutes(60));

    private final String cacheName;
    private final Duration ttl;

    CacheType(String cacheName, Duration ttl) {
        this.cacheName = cacheName;
        this.ttl = ttl;
    }

    /**
     * @Cacheable, @CacheEvict 등의 어노테이션에서 직접 사용하기 위한 상수 클래스
     */
    public static final class Names {
        public static final String ALL_PRODUCTS = "allProducts";
        public static final String PRODUCT = "product";
        public static final String POPULAR_PRODUCTS_SALES = "popularProducts:SALES";
        public static final String POPULAR_PRODUCTS_VIEWS = "popularProducts:VIEWS";
        public static final String ORDER = "order";
        public static final String ALL_COUPONS = "allCoupons";

        /**
         * 인기 상품 캐시 이름을 type에 따라 반환합니다.
         * SpEL 표현식에서 사용: "#{T(com.example.ecommerceapi.common.config.CacheType$Names).getPopularProductsCacheName(#type)}"
         */
        public static String getPopularProductsCacheName(String type) {
            if ("SALES".equals(type)) {
                return POPULAR_PRODUCTS_SALES;
            } else if ("VIEWS".equals(type)) {
                return POPULAR_PRODUCTS_VIEWS;
            }
            throw new IllegalArgumentException("Invalid type: " + type);
        }
    }
}
