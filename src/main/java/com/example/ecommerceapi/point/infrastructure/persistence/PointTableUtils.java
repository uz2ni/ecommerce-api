package com.example.ecommerceapi.point.infrastructure.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PointTableUtils {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Point 테이블 데이터 초기화 + AUTO_INCREMENT 1로 초기화
     */
    public void resetPointTable() {
        jdbcTemplate.execute("TRUNCATE TABLE point");
    }
}
