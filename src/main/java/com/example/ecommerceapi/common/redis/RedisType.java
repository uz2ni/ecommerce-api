package com.example.ecommerceapi.common.redis;

import java.time.Duration;

/**
 * Redis 저장소 타입 인터페이스
 * Cache와 Storage(Sorted Set 등)의 공통 추상화
 */
public sealed interface RedisType permits CacheType, StorageType {

    /**
     * Redis key 이름 반환
     */
    String getKey();

    /**
     * TTL(Time To Live) 반환
     */
    Duration getTtl();
}
