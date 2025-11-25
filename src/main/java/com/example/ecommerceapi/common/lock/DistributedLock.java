package com.example.ecommerceapi.common.lock;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * 분산 락을 적용하기 위한 어노테이션입니다.
 * SpEL 표현식을 사용하여 락 키를 동적으로 지정할 수 있습니다.
 *
 * 사용 예시:
 * {@code @DistributedLock(key = "'point:' + #userId", waitTime = 3, leaseTime = 5)}
 * {@code @DistributedLock(key = "#request.couponId", type = LockType.SPIN)}
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributedLock {

    /**
     * 락 키를 추출하기 위한 SpEL 표현식
     * 예: "#userId", "#request.userId", "'coupon:' + #couponId"
     */
    String key();

    /**
     * 락 타입 (기본값: SIMPLE)
     */
    LockType type() default LockType.SIMPLE;

    /**
     * 락 획득 대기 시간 (기본값: 5초)
     */
    long waitTime() default 5L;

    /**
     * 락 보유 시간 (기본값: 3초)
     * 이 시간이 지나면 자동으로 락이 해제됩니다.
     */
    long leaseTime() default 3L;

    /**
     * 시간 단위 (기본값: 초)
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;
}
