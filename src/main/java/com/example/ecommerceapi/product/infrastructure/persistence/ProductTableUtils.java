package com.example.ecommerceapi.product.infrastructure.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductTableUtils {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Product 테이블 데이터 초기화 + AUTO_INCREMENT 1로 초기화
     */
    public void resetProductTable() {
        jdbcTemplate.execute("TRUNCATE TABLE product");
    }

}
