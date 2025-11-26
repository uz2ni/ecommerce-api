package com.example.ecommerceapi.common.lock;

import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * 락 타입에 따른 전략을 제공하는 팩토리입니다.
 */
@Component
public class LockStrategyFactory {

    private final Map<LockType, LockStrategy> strategyMap;

    public LockStrategyFactory(List<LockStrategy> strategies) {
        this.strategyMap = new EnumMap<>(LockType.class);
        for (LockStrategy strategy : strategies) {
            strategyMap.put(strategy.getLockType(), strategy);
        }
    }

    /**
     * 락 타입에 해당하는 전략을 반환합니다.
     *
     * @param lockType 락 타입
     * @return 락 전략
     * @throws IllegalArgumentException 지원하지 않는 락 타입인 경우
     */
    public LockStrategy getStrategy(LockType lockType) {
        LockStrategy strategy = strategyMap.get(lockType);
        if (strategy == null) {
            throw new IllegalArgumentException("Unsupported lock type: " + lockType);
        }
        return strategy;
    }
}
