package com.example.ecommerceapi.order.application.listener;

import com.example.ecommerceapi.order.application.publisher.OrderMessagePublisher;
import com.example.ecommerceapi.order.domain.event.OrderPaidEvent;
import com.example.ecommerceapi.order.infrastructure.kafka.dto.OrderKafkaMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderEventListener 단위 테스트")
class OrderEventListenerTest {

    @Mock
    private OrderMessagePublisher orderMessagePublisher;

    @InjectMocks
    private OrderEventListener orderEventListener;

    private OrderPaidEvent orderPaidEvent;
    private LocalDateTime paidAt;

    @BeforeEach
    void setUp() {
        paidAt = LocalDateTime.now();

        orderPaidEvent = new OrderPaidEvent(
                1,
                100,
                List.of(
                        new OrderPaidEvent.OrderItemDto(1, 2),
                        new OrderPaidEvent.OrderItemDto(2, 1)
                ),
                paidAt
        );
    }

    @Nested
    @DisplayName("이벤트 처리 성공 테스트")
    class HandleEventSuccessTest {

        @Test
        @DisplayName("OrderPaidEvent를 받아 메시지를 발행한다")
        void handleOrderPaidEvent_ShouldPublishMessage() {
            // given
            doNothing().when(orderMessagePublisher).publishOrderPaidMessage(any(OrderKafkaMessage.class));

            // when
            orderEventListener.handleOrderPaidEvent(orderPaidEvent);

            // then
            verify(orderMessagePublisher).publishOrderPaidMessage(any(OrderKafkaMessage.class));
        }

        @Test
        @DisplayName("OrderPaidEvent를 OrderKafkaMessage로 변환한다")
        void handleOrderPaidEvent_ShouldConvertToKafkaMessage() {
            // given
            ArgumentCaptor<OrderKafkaMessage> messageCaptor = ArgumentCaptor.forClass(OrderKafkaMessage.class);
            doNothing().when(orderMessagePublisher).publishOrderPaidMessage(messageCaptor.capture());

            // when
            orderEventListener.handleOrderPaidEvent(orderPaidEvent);

            // then
            OrderKafkaMessage capturedMessage = messageCaptor.getValue();
            assertThat(capturedMessage).isNotNull();
            assertThat(capturedMessage.getOrderId()).isEqualTo(1);
            assertThat(capturedMessage.getUserId()).isEqualTo(100);
            assertThat(capturedMessage.getPaidAt()).isEqualTo(paidAt);
            assertThat(capturedMessage.getOrderItems()).hasSize(2);
        }

        @Test
        @DisplayName("주문 아이템 정보를 올바르게 변환한다")
        void handleOrderPaidEvent_ShouldConvertOrderItemsCorrectly() {
            // given
            ArgumentCaptor<OrderKafkaMessage> messageCaptor = ArgumentCaptor.forClass(OrderKafkaMessage.class);
            doNothing().when(orderMessagePublisher).publishOrderPaidMessage(messageCaptor.capture());

            // when
            orderEventListener.handleOrderPaidEvent(orderPaidEvent);

            // then
            OrderKafkaMessage capturedMessage = messageCaptor.getValue();
            List<OrderKafkaMessage.OrderItemInfo> orderItems = capturedMessage.getOrderItems();

            assertThat(orderItems).hasSize(2);
            assertThat(orderItems.get(0).getProductId()).isEqualTo(1);
            assertThat(orderItems.get(0).getOrderQuantity()).isEqualTo(2);
            assertThat(orderItems.get(1).getProductId()).isEqualTo(2);
            assertThat(orderItems.get(1).getOrderQuantity()).isEqualTo(1);
        }

        @Test
        @DisplayName("단일 주문 아이템도 올바르게 처리한다")
        void handleOrderPaidEvent_ShouldHandleSingleOrderItem() {
            // given
            OrderPaidEvent singleItemEvent = new OrderPaidEvent(
                    2,
                    200,
                    List.of(new OrderPaidEvent.OrderItemDto(3, 5)),
                    paidAt
            );

            ArgumentCaptor<OrderKafkaMessage> messageCaptor = ArgumentCaptor.forClass(OrderKafkaMessage.class);
            doNothing().when(orderMessagePublisher).publishOrderPaidMessage(messageCaptor.capture());

            // when
            orderEventListener.handleOrderPaidEvent(singleItemEvent);

            // then
            OrderKafkaMessage capturedMessage = messageCaptor.getValue();
            assertThat(capturedMessage.getOrderId()).isEqualTo(2);
            assertThat(capturedMessage.getUserId()).isEqualTo(200);
            assertThat(capturedMessage.getOrderItems()).hasSize(1);
            assertThat(capturedMessage.getOrderItems().get(0).getProductId()).isEqualTo(3);
            assertThat(capturedMessage.getOrderItems().get(0).getOrderQuantity()).isEqualTo(5);
        }

        @Test
        @DisplayName("여러 주문 아이템을 모두 포함하여 발행한다")
        void handleOrderPaidEvent_ShouldIncludeAllOrderItems() {
            // given
            OrderPaidEvent multipleItemsEvent = new OrderPaidEvent(
                    3,
                    300,
                    List.of(
                            new OrderPaidEvent.OrderItemDto(1, 1),
                            new OrderPaidEvent.OrderItemDto(2, 2),
                            new OrderPaidEvent.OrderItemDto(3, 3)
                    ),
                    paidAt
            );

            ArgumentCaptor<OrderKafkaMessage> messageCaptor = ArgumentCaptor.forClass(OrderKafkaMessage.class);
            doNothing().when(orderMessagePublisher).publishOrderPaidMessage(messageCaptor.capture());

            // when
            orderEventListener.handleOrderPaidEvent(multipleItemsEvent);

            // then
            OrderKafkaMessage capturedMessage = messageCaptor.getValue();
            assertThat(capturedMessage.getOrderItems()).hasSize(3);
        }
    }

    @Nested
    @DisplayName("이벤트 처리 실패 테스트")
    class HandleEventFailureTest {

        @Test
        @DisplayName("메시지 발행 실패 시 예외를 로그만 남기고 전파하지 않는다")
        void handleOrderPaidEvent_ShouldNotThrowException_WhenPublishFails() {
            // given
            RuntimeException exception = new RuntimeException("Kafka publish failed");
            doThrow(exception).when(orderMessagePublisher).publishOrderPaidMessage(any(OrderKafkaMessage.class));

            // when & then - 예외가 발생하지 않아야 함
            assertThatCode(() ->
                    orderEventListener.handleOrderPaidEvent(orderPaidEvent)
            ).doesNotThrowAnyException();

            verify(orderMessagePublisher).publishOrderPaidMessage(any(OrderKafkaMessage.class));
        }

        @Test
        @DisplayName("네트워크 오류 발생 시에도 예외를 전파하지 않는다")
        void handleOrderPaidEvent_ShouldNotThrowException_WhenNetworkError() {
            // given
            RuntimeException exception = new RuntimeException("Network error");
            doThrow(exception).when(orderMessagePublisher).publishOrderPaidMessage(any(OrderKafkaMessage.class));

            // when & then
            assertThatCode(() ->
                    orderEventListener.handleOrderPaidEvent(orderPaidEvent)
            ).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Kafka 브로커 장애 시에도 예외를 전파하지 않는다")
        void handleOrderPaidEvent_ShouldNotThrowException_WhenKafkaBrokerDown() {
            // given
            RuntimeException exception = new RuntimeException("Kafka broker unavailable");
            doThrow(exception).when(orderMessagePublisher).publishOrderPaidMessage(any(OrderKafkaMessage.class));

            // when & then
            assertThatCode(() ->
                    orderEventListener.handleOrderPaidEvent(orderPaidEvent)
            ).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("이벤트 리스너 동작 검증 테스트")
    class EventListenerBehaviorTest {

        @Test
        @DisplayName("이벤트를 정확히 한 번만 처리한다")
        void handleOrderPaidEvent_ShouldProcessEventOnce() {
            // given
            doNothing().when(orderMessagePublisher).publishOrderPaidMessage(any(OrderKafkaMessage.class));

            // when
            orderEventListener.handleOrderPaidEvent(orderPaidEvent);

            // then
            verify(orderMessagePublisher, times(1)).publishOrderPaidMessage(any(OrderKafkaMessage.class));
        }

        @Test
        @DisplayName("여러 이벤트를 각각 독립적으로 처리한다")
        void handleOrderPaidEvent_ShouldProcessMultipleEventsIndependently() {
            // given
            OrderPaidEvent event1 = new OrderPaidEvent(
                    1,
                    100,
                    List.of(new OrderPaidEvent.OrderItemDto(1, 1)),
                    paidAt
            );

            OrderPaidEvent event2 = new OrderPaidEvent(
                    2,
                    200,
                    List.of(new OrderPaidEvent.OrderItemDto(2, 2)),
                    paidAt
            );

            ArgumentCaptor<OrderKafkaMessage> messageCaptor = ArgumentCaptor.forClass(OrderKafkaMessage.class);
            doNothing().when(orderMessagePublisher).publishOrderPaidMessage(messageCaptor.capture());

            // when
            orderEventListener.handleOrderPaidEvent(event1);
            orderEventListener.handleOrderPaidEvent(event2);

            // then
            verify(orderMessagePublisher, times(2)).publishOrderPaidMessage(any(OrderKafkaMessage.class));

            List<OrderKafkaMessage> capturedMessages = messageCaptor.getAllValues();
            assertThat(capturedMessages).hasSize(2);
            assertThat(capturedMessages.get(0).getOrderId()).isEqualTo(1);
            assertThat(capturedMessages.get(1).getOrderId()).isEqualTo(2);
        }
    }
}
