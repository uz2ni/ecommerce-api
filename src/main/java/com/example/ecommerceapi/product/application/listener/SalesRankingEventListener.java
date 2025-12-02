package com.example.ecommerceapi.product.application.listener;

import com.example.ecommerceapi.order.domain.event.OrderPaidEvent;
import com.example.ecommerceapi.product.application.service.RankingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDate;

/**
 * 주문 결제 완료 이벤트를 처리하여 일간/주간 판매 랭킹을 업데이트합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SalesRankingEventListener {
    private final RankingService rankingService;

    /**
     * 주문 결제 완료 이벤트 처리
     * 비동기로 실행되어 메인 결제 로직에 영향을 주지 않습니다.
     *
     * @param event 주문 결제 완료 이벤트
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void handleOrderPaidEvent(OrderPaidEvent event) {
        log.info("Processing OrderPaidEvent for orderId: {}", event.orderId());

        try {
            LocalDate paidDate = event.paidAt().toLocalDate();

            // 주문의 모든 상품에 대해 일간/주간 판매량 증가
            for (OrderPaidEvent.OrderItemDto item : event.orderItems()) {
                // 일간 판매 랭킹 업데이트
                rankingService.incrementDailySalesCount(
                        item.productId(),
                        item.orderQuantity(),
                        paidDate
                );

                // 주간 판매 랭킹 업데이트
                rankingService.incrementWeeklySalesCount(
                        item.productId(),
                        item.orderQuantity(),
                        paidDate
                );

                log.debug("Updated sales rankings (daily/weekly) for product {} by {} units",
                        item.productId(), item.orderQuantity());
            }

            log.info("Successfully updated sales rankings for {} products in orderId: {}",
                    event.orderItems().size(), event.orderId());
        } catch (Exception e) {
            // 랭킹 업데이트 실패는 메인 결제 로직에 영향을 주지 않음
            log.error("Failed to update sales rankings for orderId {}: {}",
                    event.orderId(), e.getMessage(), e);
        }
    }
}
