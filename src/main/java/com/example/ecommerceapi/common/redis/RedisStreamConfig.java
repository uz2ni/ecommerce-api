package com.example.ecommerceapi.common.redis;

import com.example.ecommerceapi.coupon.infrastructure.stream.config.CouponStreamConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;

import java.time.Duration;

/**
 * Redis Stream 공통 설정
 * - Stream 메시지를 수신하기 위한 Listener Container 설정
 * - Subscription 등록 및 관리
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class RedisStreamConfig {

    private final RedisConnectionFactory connectionFactory;

    /**
     * Redis Stream Message Listener Container 설정
     * - Stream으로부터 메시지를 수신하는 Container
     * - 여러 도메인의 Stream Listener가 이 Container를 공유하여 사용
     * - Subscription을 등록하고 Container를 반환 (시작은 CouponStreamInitializer에서 수행)
     */
    @Bean(destroyMethod = "stop")
    @SuppressWarnings("unchecked")
    public StreamMessageListenerContainer<String, MapRecord<String, String, String>> streamMessageListenerContainer(
            @Lazy StreamListener<String, MapRecord<String, String, String>> couponEventConsumer) {

        StreamMessageListenerContainer.StreamMessageListenerContainerOptions<String, ?> options =
                StreamMessageListenerContainer.StreamMessageListenerContainerOptions
                        .builder()
                        .pollTimeout(Duration.ofMillis(100))  // polling 주기
                        .errorHandler(t -> log.error("Error in stream listener: {}", t.getMessage(), t))
                        .build();

        StreamMessageListenerContainer<String, MapRecord<String, String, String>> container =
                (StreamMessageListenerContainer<String, MapRecord<String, String, String>>)
                        StreamMessageListenerContainer.create(connectionFactory, options);

        // Coupon Stream Subscription 등록
        registerCouponStreamSubscription(container, couponEventConsumer);

        log.info("StreamMessageListenerContainer created and subscriptions registered");
        return container;
    }

    /**
     * 쿠폰 발급 Stream Subscription 등록
     * - CouponStreamSubscription의 역할을 여기서 통합 수행
     */
    private void registerCouponStreamSubscription(
            StreamMessageListenerContainer<String, MapRecord<String, String, String>> container,
            StreamListener<String, MapRecord<String, String, String>> couponEventConsumer) {

        Consumer consumer = Consumer.from(
            CouponStreamConstants.COUPON_CONSUMER_GROUP,
            CouponStreamConstants.COUPON_CONSUMER_NAME
        );

        StreamOffset<String> streamOffset = StreamOffset.create(
            CouponStreamConstants.COUPON_STREAM_KEY,
            ReadOffset.lastConsumed()
        );

        container.receive(consumer, streamOffset, couponEventConsumer);

        log.info("Redis Stream Subscription registered: stream={}, group={}, consumer={}",
                CouponStreamConstants.COUPON_STREAM_KEY,
                CouponStreamConstants.COUPON_CONSUMER_GROUP,
                CouponStreamConstants.COUPON_CONSUMER_NAME);
    }
}
