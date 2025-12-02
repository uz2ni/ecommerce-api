package com.example.ecommerceapi.common.redis;

import lombok.Getter;

import java.time.Duration;
import java.time.LocalDate;

/**
 * Redis Storage 타입 정의 Enum (Sorted Set, List 등)
 * 각 저장소의 key prefix와 TTL을 중앙에서 관리합니다.
 */
@Getter
public enum StorageType implements RedisType {

    // Ranking Sorted Set
    DAILY_SALES_RANKING("store:ranking:sales:daily", Duration.ofDays(2)),
    WEEKLY_SALES_RANKING("store:ranking:sales:weekly", Duration.ofDays(8));

    private final String keyPrefix;
    private final Duration ttl;

    StorageType(String keyPrefix, Duration ttl) {
        this.keyPrefix = keyPrefix;
        this.ttl = ttl;
    }

    @Override
    public String getKey() {
        return keyPrefix;
    }

    /**
     * 날짜 기반 key 생성 (일간 랭킹용)
     * 예: "store:ranking:sales:daily:2025-12-02"
     */
    public String getKeyWithDate(LocalDate date) {
        return keyPrefix + ":" + date;
    }

    /**
     * 주차 기반 key 생성 (주간 랭킹용)
     * 예: "store:ranking:sales:weekly:2025-W48"
     */
    public String getKeyWithWeek(String weekKey) {
        return keyPrefix + ":" + weekKey;
    }

}
