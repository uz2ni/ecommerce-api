package com.example.ecommerceapi.order.infrastructure.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderItemTableUtils {

    private final JdbcTemplate jdbcTemplate;

    /**
     * OrderItem 테이블 데이터 초기화 + AUTO_INCREMENT 1로 초기화
     */
    public void resetOrderItemTable() {
        jdbcTemplate.execute("TRUNCATE TABLE order_item");
    }

}
