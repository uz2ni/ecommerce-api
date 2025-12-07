package com.example.ecommerceapi.coupon.infrastructure.stream.config;

import com.example.ecommerceapi.common.redis.RedisStreamManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.stereotype.Component;

/**
 * 쿠폰 Redis Stream 초기화
 * - 애플리케이션 시작 시 Consumer Group 생성 및 Container 시작
 * - ContextRefreshedEvent를 사용하여 모든 Bean 초기화 후 실행
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CouponStreamInitializer implements ApplicationListener<ContextRefreshedEvent> {

    private final RedisStreamManager redisStreamManager;
    private final StreamMessageListenerContainer<String, MapRecord<String, String, String>> container;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        // 1. Consumer Group 생성
        redisStreamManager.createConsumerGroup(
                CouponStreamConstants.COUPON_STREAM_KEY,
                CouponStreamConstants.COUPON_CONSUMER_GROUP
        );

        // 2. Container 시작
        if (!container.isRunning()) {
            container.start();
            log.info("StreamMessageListenerContainer started");
        }
    }
}
