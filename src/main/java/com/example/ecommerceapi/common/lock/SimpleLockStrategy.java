package com.example.ecommerceapi.common.lock;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 기본 Redisson RLock 기반 분산 락 전략입니다.
 * Pub/Sub 메커니즘을 사용하여 락 해제를 감지합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SimpleLockStrategy implements LockStrategy {

    private static final String LOCK_PREFIX = "simple-lock:";

    private final RedissonClient redissonClient;

    @Override
    public boolean tryLock(String key, long waitTime, long leaseTime, TimeUnit timeUnit) throws InterruptedException {
        RLock lock = redissonClient.getLock(LOCK_PREFIX + key);
        boolean acquired = lock.tryLock(0, leaseTime, timeUnit);

        if (acquired) {
            log.debug("[SimpleLock] Lock acquired: {}", key);
        } else {
            log.debug("[SimpleLock] Failed to acquire lock: {}", key);
        }

        return acquired;
    }

    @Override
    public void unlock(String key) {
        RLock lock = redissonClient.getLock(LOCK_PREFIX + key);
        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
            log.debug("[SimpleLock] Lock released: {}", key);
        }
    }

    @Override
    public LockType getLockType() {
        return LockType.SIMPLE;
    }
}
