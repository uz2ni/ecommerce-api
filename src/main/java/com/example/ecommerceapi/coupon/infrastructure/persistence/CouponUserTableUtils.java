package com.example.ecommerceapi.coupon.infrastructure.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CouponUserTableUtils {

    private final JdbcTemplate jdbcTemplate;

    /**
     * CouponUser 테이블 데이터 초기화 + AUTO_INCREMENT 1로 초기화
     */
    public void resetCouponUserTable() {
        jdbcTemplate.execute("TRUNCATE TABLE coupon_user");
    }

}
