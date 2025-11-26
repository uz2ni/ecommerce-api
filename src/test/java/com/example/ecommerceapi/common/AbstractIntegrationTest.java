package com.example.ecommerceapi.common;

import com.redis.testcontainers.RedisContainer;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;

/**
 * 통합 테스트를 위한 추상 클래스
 * Singleton Testcontainers를 사용하여 모든 테스트가 하나의 MySQL, Redis 컨테이너 공유
 * 이를 통해 테스트 실행 속도를 크게 향상시킴
 */
public abstract class AbstractIntegrationTest {

    @Autowired(required = false)
    private RedissonClient redissonClient;

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

    /**
     * Redis에 남아있는 분산 락 키를 모두 삭제합니다.
     * 이전 테스트에서 완전히 해제되지 않은 락으로 인한 테스트 실패를 방지합니다.
     *
     * 사용법: 각 테스트 클래스의 @BeforeEach에서 호출
     */
    protected void clearRedisLocks() {
        if (redissonClient == null) {
            return;
        }

        try {
            redissonClient.getKeys().deleteByPattern("simple-lock:*");
            redissonClient.getKeys().deleteByPattern("spin-lock:*");
            redissonClient.getKeys().deleteByPattern("pubsub-lock:*");
            redissonClient.getKeys().deleteByPattern("multi-lock:*");
        } catch (Exception e) {
            System.err.println("Failed to clear Redis locks: " + e.getMessage());
        }
    }

    /**
     * Redis의 모든 키를 삭제합니다.
     * 테스트 간 완전한 격리가 필요한 경우 사용합니다.
     */
    protected void clearAllRedisKeys() {
        if (redissonClient == null) {
            return;
        }

        try {
            redissonClient.getKeys().flushall();
        } catch (Exception e) {
            System.err.println("Failed to clear all Redis keys: " + e.getMessage());
        }
    }
}