package com.example.ecommerceapi.common.lock;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Spin Lock 방식의 분산 락 전략입니다.
 * 짧은 간격으로 폴링하여 락 획득을 시도합니다.
 * 락 보유 시간이 매우 짧을 것으로 예상될 때 적합합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SpinLockStrategy implements LockStrategy {

    private static final String LOCK_PREFIX = "spin-lock:";
    private static final long SPIN_INTERVAL_MS = 50; // 50ms 간격으로 재시도

    private final RedissonClient redissonClient;

    @Override
    public boolean tryLock(String key, long waitTime, long leaseTime, TimeUnit timeUnit) throws InterruptedException {
        RLock lock = redissonClient.getLock(LOCK_PREFIX + key);
        long waitTimeMs = timeUnit.toMillis(waitTime);
        long startTime = System.currentTimeMillis();

        // Spin 방식: 짧은 간격으로 반복적으로 락 획득 시도
        while (System.currentTimeMillis() - startTime < waitTimeMs) {
            boolean acquired = lock.tryLock(0, leaseTime, timeUnit);

            if (acquired) {
                log.debug("[SpinLock] Lock acquired: {} after {}ms", key, System.currentTimeMillis() - startTime);
                return true;
            }

            // SPIN_INTERVAL_MS 만큼 대기 후 재시도
            Thread.sleep(SPIN_INTERVAL_MS);
        }

        log.debug("[SpinLock] Failed to acquire lock: {} after {}ms", key, waitTimeMs);
        return false;
    }

    @Override
    public void unlock(String key) {
        RLock lock = redissonClient.getLock(LOCK_PREFIX + key);
        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
            log.debug("[SpinLock] Lock released: {}", key);
        }
    }

    @Override
    public LockType getLockType() {
        return LockType.SPIN;
    }
}
