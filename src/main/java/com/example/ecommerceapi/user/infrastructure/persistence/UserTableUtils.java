package com.example.ecommerceapi.user.infrastructure.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserTableUtils {

    private final JdbcTemplate jdbcTemplate;

    /**
     * User 테이블 데이터 초기화 + AUTO_INCREMENT 1로 초기화
     */
    public void resetUserTable() {
        jdbcTemplate.execute("TRUNCATE TABLE user");
    }

}