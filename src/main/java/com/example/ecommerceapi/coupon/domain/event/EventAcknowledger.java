package com.example.ecommerceapi.coupon.domain.event;

import org.springframework.data.redis.connection.stream.RecordId;

/**
 * 이벤트 메시지 ACK 처리 인터페이스
 * - 이벤트 Consumer에서 메시지 처리 완료를 알리는 역할
 */
public interface EventAcknowledger {

    /**
     * 메시지 ACK 처리
     *
     * @param recordId 처리 완료된 메시지 ID
     */
    void acknowledge(RecordId recordId);
}
