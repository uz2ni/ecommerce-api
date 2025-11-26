package com.example.ecommerceapi.common.lock;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Spin Lock 방식의 분산 락 전략입니다.
 * Redisson의 SpinLock 구현을 활용하여 락 획득을 시도합니다.
 * 락 보유 시간이 매우 짧을 것으로 예상될 때 적합합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SpinLockStrategy implements LockStrategy {

    private static final String LOCK_PREFIX = "spin-lock:";

    private final RedissonClient redissonClient;

    @Override
    public boolean tryLock(String key, long waitTime, long leaseTime, TimeUnit timeUnit) throws InterruptedException {
        RLock spinLock = redissonClient.getSpinLock(LOCK_PREFIX + key);
        boolean acquired = spinLock.tryLock(waitTime, leaseTime, timeUnit);

        if (acquired) {
            log.debug("[SpinLock] Lock acquired: {}", key);
        } else {
            log.debug("[SpinLock] Failed to acquire lock: {}", key);
        }
        return acquired;
    }

    @Override
    public void unlock(String key) {
        RLock spinLock = redissonClient.getSpinLock(LOCK_PREFIX + key);
        if (spinLock.isHeldByCurrentThread()) {
            spinLock.unlock();
            log.debug("[SpinLock] Lock released: {}", key);
        }
    }

    @Override
    public LockType getLockType() {
        return LockType.SPIN;
    }
}
