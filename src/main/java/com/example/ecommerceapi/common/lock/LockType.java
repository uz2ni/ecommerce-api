package com.example.ecommerceapi.common.lock;

/**
 * 분산 락 타입을 정의합니다.
 */
public enum LockType {

    /**
     * 기본 Redisson RLock 사용
     * - 일반적인 동시성 제어에 적합
     * - 특정 키가 없으면 락 획득 성공, 있으면 실패
     */
    SIMPLE,

    /**
     * Spin Lock 방식
     * - 짧은 간격으로 폴링하여 락 획득 시도
     * - 락 보유 시간이 매우 짧을 때 적합
     * - CPU 사용량이 높을 수 있음
     */
    SPIN,

    /**
     * Pub/Sub Lock 방식
     * - 대기,이벤트 기반 재시도 처리
     * - 긴 대기 시간에 적합, CPU 절약
     */
    PUB_SUB,

    /**
     * Multi Lock 방식
     * - 여러 리소스에 대한 동시 락 획득
     * - 모든 락을 획득해야 성공
     * - 데드락 방지 메커니즘 포함
     */
    MULTI
}
