package com.example.ecommerceapi.product.application.listener;

import com.example.ecommerceapi.order.domain.event.OrderPaidEvent;
import com.example.ecommerceapi.product.application.service.RankingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SalesRankingEventListener 단위 테스트")
class SalesRankingEventListenerTest {

    @Mock
    private RankingService rankingService;

    @InjectMocks
    private SalesRankingEventListener salesRankingEventListener;

    @Test
    @DisplayName("주문 결제 완료 이벤트 처리 - 일간/주간 판매량 증가")
    void handleOrderPaidEvent_ShouldIncrementSalesRankings() {
        // given
        LocalDateTime paidAt = LocalDateTime.of(2025, 12, 3, 10, 30);
        LocalDate paidDate = paidAt.toLocalDate();

        OrderPaidEvent.OrderItemDto item1 = new OrderPaidEvent.OrderItemDto(1, 2);
        OrderPaidEvent.OrderItemDto item2 = new OrderPaidEvent.OrderItemDto(2, 1);
        List<OrderPaidEvent.OrderItemDto> items = Arrays.asList(item1, item2);

        OrderPaidEvent event = new OrderPaidEvent(
                1,
                1,
                items,
                paidAt
        );

        // when
        salesRankingEventListener.handleOrderPaidEvent(event);

        // then
        verify(rankingService).incrementDailySalesCount(1, 2, paidDate);
        verify(rankingService).incrementDailySalesCount(2, 1, paidDate);
        verify(rankingService).incrementWeeklySalesCount(1, 2, paidDate);
        verify(rankingService).incrementWeeklySalesCount(2, 1, paidDate);
    }

    @Test
    @DisplayName("주문 결제 완료 이벤트 처리 - 단일 상품")
    void handleOrderPaidEvent_ShouldIncrementSalesRankings_WithSingleProduct() {
        // given
        LocalDateTime paidAt = LocalDateTime.now();
        LocalDate paidDate = paidAt.toLocalDate();

        OrderPaidEvent.OrderItemDto item = new OrderPaidEvent.OrderItemDto(5, 3);
        List<OrderPaidEvent.OrderItemDto> items = Arrays.asList(item);

        OrderPaidEvent event = new OrderPaidEvent(
                10,
                2,
                items,
                paidAt
        );

        // when
        salesRankingEventListener.handleOrderPaidEvent(event);

        // then
        verify(rankingService).incrementDailySalesCount(5, 3, paidDate);
        verify(rankingService).incrementWeeklySalesCount(5, 3, paidDate);
    }

    @Test
    @DisplayName("주문 결제 완료 이벤트 처리 - 빈 주문 항목")
    void handleOrderPaidEvent_ShouldNotIncrementSalesRankings_WhenNoItems() {
        // given
        LocalDateTime paidAt = LocalDateTime.now();
        List<OrderPaidEvent.OrderItemDto> items = Arrays.asList();

        OrderPaidEvent event = new OrderPaidEvent(
                20,
                3,
                items,
                paidAt
        );

        // when
        salesRankingEventListener.handleOrderPaidEvent(event);

        // then
        verifyNoInteractions(rankingService);
    }

    @Test
    @DisplayName("주문 결제 완료 이벤트 처리 - 랭킹 업데이트 실패 시 예외를 삼킨다")
    void handleOrderPaidEvent_ShouldNotThrowException_WhenRankingUpdateFails() {
        // given
        LocalDateTime paidAt = LocalDateTime.now();
        LocalDate paidDate = paidAt.toLocalDate();

        OrderPaidEvent.OrderItemDto item = new OrderPaidEvent.OrderItemDto(1, 1);
        List<OrderPaidEvent.OrderItemDto> items = Arrays.asList(item);

        OrderPaidEvent event = new OrderPaidEvent(
                1,
                1,
                items,
                paidAt
        );

        doThrow(new RuntimeException("Redis connection failed"))
                .when(rankingService).incrementDailySalesCount(anyInt(), anyInt(), any(LocalDate.class));

        // when & then - 예외가 밖으로 전파되지 않아야 함
        salesRankingEventListener.handleOrderPaidEvent(event);

        verify(rankingService).incrementDailySalesCount(1, 1, paidDate);
        // incrementWeeklySalesCount는 호출되지 않음 (daily에서 예외 발생)
    }
}
