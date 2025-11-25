package com.example.ecommerceapi.common.lock;

import com.example.ecommerceapi.common.exception.ErrorCode;
import com.example.ecommerceapi.common.exception.LockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

/**
 * @DistributedLock 어노테이션이 적용된 메서드에 분산 락을 적용하는 Aspect입니다.
 * 트랜잭션보다 먼저 실행되어야 하므로 Order를 낮게 설정합니다.
 */
@Slf4j
@Aspect
@Component
@Order(1) // 트랜잭션(@Transactional)보다 먼저 실행
@RequiredArgsConstructor
public class DistributedLockAspect {

    private final LockStrategyFactory lockStrategyFactory;
    private final ExpressionParser parser = new SpelExpressionParser();

    @Around("@annotation(distributedLock)")
    public Object around(ProceedingJoinPoint joinPoint, DistributedLock distributedLock) throws Throwable {
        String lockKey = extractLockKey(joinPoint, distributedLock.key());
        LockStrategy strategy = lockStrategyFactory.getStrategy(distributedLock.type());

        boolean acquired = false;
        try {
            acquired = strategy.tryLock(
                    lockKey,
                    distributedLock.waitTime(),
                    distributedLock.leaseTime(),
                    distributedLock.timeUnit()
            );

            if (!acquired) {
                log.error("Failed to acquire distributed lock: key={}, type={}", lockKey, distributedLock.type());
                throw new LockException(ErrorCode.LOCK_TIMEOUT);
            }

            log.debug("Distributed lock acquired: key={}, type={}", lockKey, distributedLock.type());
            return joinPoint.proceed();

        } finally {
            if (acquired) {
                try {
                    strategy.unlock(lockKey);
                    log.debug("Distributed lock released: key={}, type={}", lockKey, distributedLock.type());
                } catch (Exception e) {
                    log.warn("Failed to release distributed lock: key={}", lockKey, e);
                }
            }
        }
    }

    /**
     * SpEL 표현식을 파싱하여 락 키를 추출합니다.
     */
    private String extractLockKey(ProceedingJoinPoint joinPoint, String keyExpression) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] paramNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();

        StandardEvaluationContext context = new StandardEvaluationContext();
        for (int i = 0; i < paramNames.length; i++) {
            context.setVariable(paramNames[i], args[i]);
        }

        Object key = parser.parseExpression(keyExpression).getValue(context);
        return key != null ? key.toString() : "null";
    }
}
