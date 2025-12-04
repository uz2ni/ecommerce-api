package com.example.ecommerceapi.coupon.infrastructure.stream.acknowledgement;

import com.example.ecommerceapi.coupon.domain.stream.StreamAcknowledger;
import com.example.ecommerceapi.coupon.infrastructure.stream.config.CouponStreamConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Redis Stream ACK 처리 구현체
 * - Redis Stream Consumer Group에서 메시지 처리 완료를 알림
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisStreamAcknowledger implements StreamAcknowledger {

    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public void acknowledge(RecordId recordId) {
        try {
            redisTemplate.opsForStream().acknowledge(
                    CouponStreamConstants.COUPON_STREAM_KEY,
                    CouponStreamConstants.COUPON_CONSUMER_GROUP,
                    recordId
            );
            log.debug("Message acknowledged: recordId={}", recordId);
        } catch (Exception e) {
            log.error("Failed to acknowledge message: recordId={}", recordId, e);
        }
    }
}
