package com.example.ecommerceapi.common.lock;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Pub/Sub 기반 분산 락 전략입니다.
 * Redis Pub/Sub을 사용하여 락 해제 알림을 받습니다.
 * 긴 대기 시간에 적합하며 CPU 사용량이 적습니다.
 *
 * Note: Redisson의 기본 RLock이 이미 Pub/Sub 메커니즘을 사용하므로
 * SimpleLockStrategy와 동일한 구현이지만, 명시적 구분을 위해 분리합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PubSubLockStrategy implements LockStrategy {

    private static final String LOCK_PREFIX = "pubsub-lock:";

    private final RedissonClient redissonClient;

    @Override
    public boolean tryLock(String key, long waitTime, long leaseTime, TimeUnit timeUnit) throws InterruptedException {
        RLock lock = redissonClient.getLock(LOCK_PREFIX + key);
        boolean acquired = lock.tryLock(waitTime, leaseTime, timeUnit);

        if (acquired) {
            log.debug("[PubSubLock] Lock acquired: {}", key);
        } else {
            log.debug("[PubSubLock] Failed to acquire lock: {}", key);
        }

        return acquired;
    }

    @Override
    public void unlock(String key) {
        RLock lock = redissonClient.getLock(LOCK_PREFIX + key);
        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
            log.debug("[PubSubLock] Lock released: {}", key);
        }
    }

    @Override
    public LockType getLockType() {
        return LockType.PUB_SUB;
    }
}
