package com.example.ecommerceapi.common;

import com.redis.testcontainers.RedisContainer;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;

/**
 * 통합 테스트를 위한 추상 클래스
 * Singleton Testcontainers를 사용하여 모든 테스트가 하나의 MySQL, Redis 컨테이너 공유
 * 이를 통해 테스트 실행 속도를 크게 향상시킴
 */
public abstract class AbstractIntegrationTest {

    // Singleton 패턴으로 모든 테스트가 하나의 컨테이너 공유
    private static final MySQLContainer<?> mysql;
    private static final RedisContainer redis;

    static {
        mysql = new MySQLContainer<>("mysql:8.0")
                .withDatabaseName("testdb")
                .withUsername("test")
                .withPassword("test");
        mysql.start();

        redis = new RedisContainer("redis:7.2");
        redis.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // MySQL 설정
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create");

        // Redis 설정
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }
}