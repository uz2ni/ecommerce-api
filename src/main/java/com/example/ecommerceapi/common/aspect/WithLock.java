package com.example.ecommerceapi.common.aspect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 메서드 실행 시 동시성 제어를 위한 락을 적용합니다.
 * SpEL 표현식을 사용하여 락 키를 지정할 수 있습니다.
 *
 * 사용 예시:
 * {@code @WithLock(key = "#userId", timeout = 3)}
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface WithLock {

    /**
     * 락 키를 추출하기 위한 SpEL 표현식
     * 예: "#userId", "#request.userId", "'user:' + #userId"
     */
    String key();

    /**
     * 락 획득 타임아웃 (초)
     * 기본값: 3초
     */
    long timeout() default 3L;

    /**
     * true면 락을 못 얻으면 즉시 무시, false면 timeout까지 대기 후 실패
     * 기본값: false
     */
    boolean ignoreIfLocked() default false; // true면 실패 시 바로 무시
}