package com.example.ecommerceapi.common.redis;

import java.time.Duration;

/**
 * Redis 저장소 타입 인터페이스
 * Cache와 Storage(Sorted Set 등)의 공통 추상화
 * sealed-permits : java17 추가된 문법. 인터페이스의 구현체를 지정함(다른건 상속 안됨)
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
