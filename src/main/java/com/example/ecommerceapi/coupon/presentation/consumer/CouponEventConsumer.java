package com.example.ecommerceapi.coupon.presentation.consumer;

import com.example.ecommerceapi.common.exception.CouponException;
import com.example.ecommerceapi.coupon.application.service.CouponService;
import com.example.ecommerceapi.coupon.domain.stream.StreamAcknowledger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.stereotype.Component;

/**
 * 쿠폰 발급 이벤트 Consumer
 * Redis Stream으로부터 쿠폰 발급 요청을 수신하고 처리
 * - 메시지 파싱 및 서비스 호출에만 집중
 * - 비즈니스 로직은 CouponService에 위임
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CouponEventConsumer implements StreamListener<String, MapRecord<String, String, String>> {

    private final CouponService couponService;
    private final StreamAcknowledger streamAcknowledger;

    /**
     * Redis Stream으로부터 메시지를 수신하여 쿠폰 발급 처리
     */
    @Override
    public void onMessage(MapRecord<String, String, String> message) {
        RecordId recordId = message.getId();

        try {
            // 1. 메시지 파싱
            Integer couponId = Integer.valueOf(message.getValue().get("couponId"));
            Integer userId = Integer.valueOf(message.getValue().get("userId"));

            log.info("Received coupon issue event: couponId={}, userId={}, recordId={}",
                    couponId, userId, recordId);

            // 2. 쿠폰 발급 처리 (서비스 호출)
            couponService.processCouponIssue(couponId, userId);

            // 3. ACK 처리
            streamAcknowledger.acknowledge(recordId);

            log.info("Coupon issued successfully: couponId={}, userId={}, recordId={}",
                    couponId, userId, recordId);

        } catch (CouponException e) {
            // 비즈니스 예외는 로그만 남기고 ACK 처리 (재시도 불필요)
            log.warn("Coupon issue failed (business exception): {}, recordId={}",
                    e.getMessage(), recordId);
            streamAcknowledger.acknowledge(recordId);

        } catch (Exception e) {
            // 시스템 예외는 로그 남기고 ACK 하지 않음 (재시도 가능)
            log.error("Coupon issue failed (system error): recordId={}, error={}",
                    recordId, e.getMessage(), e);
        }
    }
}
