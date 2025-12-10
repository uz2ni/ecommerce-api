package com.example.ecommerceapi.common.infrastructure.external.listener;

import com.example.ecommerceapi.common.infrastructure.external.client.ExternalLoggingClient;
import com.example.ecommerceapi.order.domain.event.OrderPaidEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 주문 결제 완료 이벤트를 외부 로깅 시스템으로 전송하는 리스너
 * 비동기로 실행되어 메인 결제 로직에 영향을 주지 않습니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExternalLoggingEventListener {

    private final ExternalLoggingClient externalLoggingClient;

    /**
     * 주문 결제 완료 이벤트를 외부 로깅 시스템으로 전송
     * 트랜잭션 커밋 후 비동기로 실행됩니다.
     *
     * @param event 주문 결제 완료 이벤트
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void handleOrderPaidEvent(OrderPaidEvent event) {
        log.info("Processing external logging for OrderPaidEvent: orderId={}", event.orderId());

        try {
            // 이벤트 데이터를 외부 로깅 형식으로 변환
            Map<String, Object> logPayload = buildLogPayload(event);

            // 외부 로깅 시스템으로 전송
            externalLoggingClient.sendLog(logPayload);

            log.info("Successfully sent OrderPaidEvent to external logging system: orderId={}",
                    event.orderId());
        } catch (Exception e) {
            // 외부 로깅 실패는 메인 비즈니스 로직에 영향을 주지 않음 (비동기라 catch문 아니어도 영향은 없음)
            // 실패 대응을 해당 시점에 명확히하기 위해 catch문 사용하여 대응
            log.error("Failed to send OrderPaidEvent to external logging system: orderId={}, error={}",
                    event.orderId(), e.getMessage(), e);
        }
    }

    /**
     * OrderPaidEvent를 외부 로깅 시스템 형식으로 변환
     */
    private Map<String, Object> buildLogPayload(OrderPaidEvent event) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("eventType", "ORDER_PAID");
        payload.put("orderId", event.orderId());
        payload.put("userId", event.userId());
        payload.put("paidAt", event.paidAt().toString());
        payload.put("orderItems", convertOrderItems(event.orderItems()));

        return payload;
    }

    /**
     * 주문 상품 목록을 Map 리스트로 변환
     */
    private List<Map<String, Object>> convertOrderItems(List<OrderPaidEvent.OrderItemDto> orderItems) {
        return orderItems.stream()
                .map(item -> {
                    Map<String, Object> itemMap = new HashMap<>();
                    itemMap.put("productId", item.productId());
                    itemMap.put("orderQuantity", item.orderQuantity());
                    return itemMap;
                })
                .collect(Collectors.toList());
    }
}
