package com.example.ecommerceapi.common.lock;

import java.util.concurrent.TimeUnit;

/**
 * 분산 락 전략 인터페이스입니다.
 * 다양한 락 방식을 통일된 인터페이스로 제공합니다.
 */
public interface LockStrategy {

    /**
     * 락 획득을 시도합니다.
     *
     * @param key       락 키
     * @param waitTime  락 획득 대기 시간
     * @param leaseTime 락 보유 시간
     * @param timeUnit  시간 단위
     * @return 락 획득 성공 여부
     * @throws InterruptedException 대기 중 인터럽트 발생 시
     */
    boolean tryLock(String key, long waitTime, long leaseTime, TimeUnit timeUnit) throws InterruptedException;

    /**
     * 락을 해제합니다.
     *
     * @param key 락 키
     */
    void unlock(String key);

    /**
     * 지원하는 락 타입을 반환합니다.
     *
     * @return 락 타입
     */
    LockType getLockType();
}
