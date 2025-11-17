package com.example.ecommerceapi.cart.infrastructure.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CartItemTableUtils {

    private final JdbcTemplate jdbcTemplate;

    /**
     * CartItem 테이블 데이터 초기화 + AUTO_INCREMENT 1로 초기화
     */
    public void resetCartItemTable() {
        jdbcTemplate.execute("TRUNCATE TABLE cart_item");
    }

}
