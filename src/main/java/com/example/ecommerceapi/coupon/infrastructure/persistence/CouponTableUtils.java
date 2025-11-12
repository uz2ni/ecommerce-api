package com.example.ecommerceapi.coupon.infrastructure.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CouponTableUtils {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Coupon 테이블 데이터 초기화 + AUTO_INCREMENT 1로 초기화
     */
    public void resetCouponTable() {
        jdbcTemplate.execute("TRUNCATE TABLE coupon");
    }

}
