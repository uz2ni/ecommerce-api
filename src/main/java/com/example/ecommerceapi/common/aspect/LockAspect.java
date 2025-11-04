package com.example.ecommerceapi.common.aspect;

import com.example.ecommerceapi.common.exception.ErrorCode;
import com.example.ecommerceapi.common.exception.LockException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @WithLock 어노테이션이 적용된 메서드에 대해 동시성 제어를 수행하는 Aspect
 */
@Slf4j
@Aspect
@Component
public class LockAspect {

    private final ConcurrentHashMap<Object, ReentrantLock> locks = new ConcurrentHashMap<>();
    private final ExpressionParser parser = new SpelExpressionParser();

    @Around("@annotation(withLock)")
    public Object executeLocked(ProceedingJoinPoint joinPoint, WithLock withLock) throws Throwable {
        Object lockKey = extractLockKey(joinPoint, withLock.key());
        ReentrantLock lock = locks.computeIfAbsent(lockKey, k -> new ReentrantLock(true));

        boolean acquired = acquireLock(lock, lockKey, withLock);
        if (!acquired) {
            // ignoreIfLocked = true일 때 락 획득 실패
            return null;
        }

        try {
            return proceedWithLock(joinPoint, lockKey);
        } finally {
            releaseLock(lock, lockKey, acquired);
        }
    }

    private boolean acquireLock(ReentrantLock lock, Object lockKey, WithLock withLock) throws InterruptedException {
        if (withLock.ignoreIfLocked()) {
            boolean acquired = lock.tryLock();
            if (!acquired) log.info("Lock busy, skipping execution for key: {}", lockKey);
            return acquired;
        } else {
            boolean acquired = lock.tryLock(withLock.timeout(), TimeUnit.SECONDS);
            if (!acquired) {
                log.error("Failed to acquire lock for key: {} within {} seconds", lockKey, withLock.timeout());
                throw new LockException(ErrorCode.LOCK_TIMEOUT);
            }
            return true;
        }
    }

    private Object proceedWithLock(ProceedingJoinPoint joinPoint, Object lockKey) throws Throwable {
        log.debug("Lock acquired for key: {}", lockKey);
        return joinPoint.proceed();
    }

    private void releaseLock(ReentrantLock lock, Object lockKey, boolean acquired) {
        if (acquired) {
            lock.unlock();
            log.debug("Lock released for key: {}", lockKey);
        }
    }

    private Object extractLockKey(ProceedingJoinPoint joinPoint, String keyExpression) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] paramNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();

        StandardEvaluationContext context = new StandardEvaluationContext();
        for (int i = 0; i < paramNames.length; i++) {
            context.setVariable(paramNames[i], args[i]);
        }

        return parser.parseExpression(keyExpression).getValue(context);
    }
}