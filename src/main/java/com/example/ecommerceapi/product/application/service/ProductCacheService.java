package com.example.ecommerceapi.product.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
 * 상품 조회수 관련 캐싱 서비스입니다.
 * Redis INCR를 사용한 Write-Behind 패턴을 구현합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductCacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    // 캐시 키 상수
    private static final String VIEW_COUNT_KEY_PREFIX = "product:viewcount:";

    /**
     * 조회수 증가 (Redis INCR)
     * @return 증가 후 조회수
     */
    public Long incrementViewCount(Integer productId) {
        String key = VIEW_COUNT_KEY_PREFIX + productId;
        Long count = redisTemplate.opsForValue().increment(key);
        log.debug("Product view count incremented: productId={}, count={}", productId, count);
        return count;
    }

    /**
     * Redis에 저장된 조회수 조회
     */
    public Long getViewCount(Integer productId) {
        String key = VIEW_COUNT_KEY_PREFIX + productId;
        Object value = redisTemplate.opsForValue().get(key);
        return value != null ? Long.parseLong(value.toString()) : 0L;
    }

    /**
     * 조회수 초기화 (DB 동기화 후)
     */
    public void deleteViewCount(Integer productId) {
        String key = VIEW_COUNT_KEY_PREFIX + productId;
        redisTemplate.delete(key);
    }

    /**
     * 모든 조회수 키 패턴 조회
     */
    public List<String> getAllViewCountKeys() {
        Set<String> keys = redisTemplate.keys(VIEW_COUNT_KEY_PREFIX + "*");
        if (keys == null) {
            return List.of();
        }
        return keys.stream().toList();
    }
}