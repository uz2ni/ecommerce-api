package com.example.ecommerceapi.common.lock;

import com.example.ecommerceapi.common.exception.ErrorCode;
import com.example.ecommerceapi.common.exception.LockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Multi Lock 방식의 분산 락 전략입니다.
 * Redisson의 MultiLock을 활용하여 여러 리소스에 대한 원자적 락 획득을 보장합니다.
 * 모든 락을 획득해야만 성공하며, 데드락 방지 메커니즘이 포함되어 있습니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MultiLockStrategy implements LockStrategy {

    private static final String LOCK_PREFIX = "multi-lock:";

    private final RedissonClient redissonClient;

    // ThreadLocal을 사용하여 현재 스레드의 MultiLock 인스턴스를 저장
    private final ThreadLocal<RLock> currentMultiLock = new ThreadLocal<>();

    @Override
    public boolean tryLock(String key, long waitTime, long leaseTime, TimeUnit timeUnit) {
        throw new LockException(ErrorCode.LOCK_NOT_SUPPORTED); // 단일 키로 호출되는 경우는 지원하지 않음
    }

    /**
     * 여러 키에 대한 락 획득을 시도합니다.
     * 모든 락을 원자적으로 획득하거나, 전부 실패합니다.
     *
     * @param keys      락 키 리스트
     * @param waitTime  락 획득 대기 시간
     * @param leaseTime 락 보유 시간
     * @param timeUnit  시간 단위
     * @return 락 획득 성공 여부
     * @throws InterruptedException 대기 중 인터럽트 발생 시
     */
    public boolean tryLockMultiple(List<String> keys, long waitTime, long leaseTime, TimeUnit timeUnit)
            throws InterruptedException {
        if (keys == null || keys.isEmpty()) {
            log.warn("[MultiLock] No keys provided");
            return false;
        }

        // 각 키에 대한 RLock 생성
        RLock[] locks = keys.stream()
                .map(key -> redissonClient.getLock(LOCK_PREFIX + key))
                .toArray(RLock[]::new);

        // MultiLock 생성
        RLock multiLock = redissonClient.getMultiLock(locks);

        boolean acquired = multiLock.tryLock(waitTime, leaseTime, timeUnit);

        if (acquired) {
            // ThreadLocal에 저장하여 unlock 시 사용
            currentMultiLock.set(multiLock);
            log.debug("[MultiLock] Lock acquired for keys: {}", keys);
        } else {
            log.debug("[MultiLock] Failed to acquire lock for keys: {}", keys);
        }

        return acquired;
    }

    @Override
    public void unlock(String key) {
        throw new LockException(ErrorCode.LOCK_NOT_SUPPORTED); // 단일 키로 호출되는 경우는 지원하지 않음
    }

    /**
     * 획득한 MultiLock을 해제합니다.
     */
    public void unlockMultiple() {
        RLock multiLock = currentMultiLock.get();

        if (multiLock != null) {
            try {
                if (multiLock.isHeldByCurrentThread()) {
                    multiLock.unlock();
                    log.debug("[MultiLock] Lock released");
                }
            } finally {
                currentMultiLock.remove();
            }
        }
    }

    @Override
    public LockType getLockType() {
        return LockType.MULTI;
    }
}