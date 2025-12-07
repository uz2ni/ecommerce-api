package com.example.ecommerceapi.coupon.infrastructure.stream.publisher;

import com.example.ecommerceapi.coupon.domain.publisher.CouponIssuePublisher;
import com.example.ecommerceapi.coupon.infrastructure.stream.config.CouponStreamConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 쿠폰 발급 이벤트 Publisher (Redis Stream 구현체)
 * Redis Stream에 쿠폰 발급 요청 이벤트를 발행
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CouponEventPublisher implements CouponIssuePublisher {

    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public String publish(Integer couponId, Integer userId) {
        // 1. Map 형태로 이벤트 데이터 생성
        Map<String, String> eventData = new HashMap<>();
        eventData.put("couponId", String.valueOf(couponId));
        eventData.put("userId", String.valueOf(userId));

        // 2. MapRecord 생성
        MapRecord<String, String, String> record = StreamRecords.newRecord()
                .in(CouponStreamConstants.COUPON_STREAM_KEY)
                .ofMap(eventData);

        // 3. Redis Stream에 발행
        RecordId recordId = redisTemplate.opsForStream().add(record);

        log.info("쿠폰 발급 event published: couponId={}, userId={}, recordId={}",
                couponId, userId, recordId);

        return recordId.getValue();
    }
}
