package com.example.ecommerceapi.order.application.publisher;

import com.example.ecommerceapi.order.infrastructure.kafka.dto.OrderKafkaMessage;

/**
 * 주문 메시지를 외부로 발행하는 퍼블리셔 인터페이스
 * 도메인 이벤트를 외부 메시징 시스템용 메시지로 변환하여 발행합니다.
 * Infrastructure 레이어에서 구현체를 제공합니다.
 */
public interface OrderMessagePublisher {

    /**
     * 주문 결제 완료 메시지를 외부로 발행
     *
     * @param message 주문 결제 완료 메시지
     */
    void publishOrderPaidMessage(OrderKafkaMessage message);
}
