package com.example.ecommerceapi.common.infrastructure.kafka.consumer;

import com.example.ecommerceapi.common.infrastructure.external.client.ExternalLoggingClient;
import com.example.ecommerceapi.common.infrastructure.kafka.dto.Ticket;
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
@DisplayName("LoggingKafkaConsumer 단위 테스트")
class LoggingKafkaConsumerTest {

    @Mock
    private ExternalLoggingClient externalLoggingClient;

    @InjectMocks
    private LoggingKafkaConsumer loggingKafkaConsumer;

    private OrderKafkaMessage orderKafkaMessage;
    private Ticket<OrderKafkaMessage> ticket;
    private String testTopic = "order-paid-events";
    private Integer testPartition = 0;
    private Long testOffset = 123L;

    @BeforeEach
    void setUp() {
        orderKafkaMessage = OrderKafkaMessage.builder()
                .orderId(1)
                .userId(100)
                .orderItems(List.of(
                        OrderKafkaMessage.OrderItemInfo.builder()
                                .productId(1)
                                .orderQuantity(2)
                                .build(),
                        OrderKafkaMessage.OrderItemInfo.builder()
                                .productId(2)
                                .orderQuantity(1)
                                .build()
                ))
                .paidAt(LocalDateTime.now())
                .build();

        ticket = new Ticket<>(testTopic, orderKafkaMessage);
    }

    @Nested
    @DisplayName("메시지 소비 성공 테스트")
    class ConsumeSuccessTest {

        @Test
        @DisplayName("Kafka 메시지를 수신하고 외부 로깅 시스템에 전송한다")
        void consumeOrderPaidEvent_ShouldSendToExternalLoggingSystem() {
            // given
            doNothing().when(externalLoggingClient).sendLog(any(OrderKafkaMessage.class));

            // when
            loggingKafkaConsumer.consumeOrderPaidEvent(ticket, testTopic, testPartition, testOffset);

            // then
            verify(externalLoggingClient).sendLog(any(OrderKafkaMessage.class));
        }

        @Test
        @DisplayName("Ticket 페이로드를 OrderKafkaMessage로 변환한다")
        void consumeOrderPaidEvent_ShouldConvertPayloadToOrderKafkaMessage() {
            // given
            ArgumentCaptor<OrderKafkaMessage> messageCaptor = ArgumentCaptor.forClass(OrderKafkaMessage.class);
            doNothing().when(externalLoggingClient).sendLog(messageCaptor.capture());

            // when
            loggingKafkaConsumer.consumeOrderPaidEvent(ticket, testTopic, testPartition, testOffset);

            // then
            OrderKafkaMessage capturedMessage = messageCaptor.getValue();
            assertThat(capturedMessage).isNotNull();
            assertThat(capturedMessage.getOrderId()).isEqualTo(1);
            assertThat(capturedMessage.getUserId()).isEqualTo(100);
            assertThat(capturedMessage.getOrderItems()).hasSize(2);
            assertThat(capturedMessage.getPaidAt()).isNotNull();
        }

        @Test
        @DisplayName("메시지를 정확히 한 번만 처리한다")
        void consumeOrderPaidEvent_ShouldProcessMessageOnce() {
            // given
            doNothing().when(externalLoggingClient).sendLog(any(OrderKafkaMessage.class));

            // when
            loggingKafkaConsumer.consumeOrderPaidEvent(ticket, testTopic, testPartition, testOffset);

            // then
            verify(externalLoggingClient, times(1)).sendLog(any(OrderKafkaMessage.class));
        }

        @Test
        @DisplayName("여러 주문 아이템이 포함된 메시지를 올바르게 처리한다")
        void consumeOrderPaidEvent_ShouldHandleMultipleOrderItems() {
            // given
            ArgumentCaptor<OrderKafkaMessage> messageCaptor = ArgumentCaptor.forClass(OrderKafkaMessage.class);
            doNothing().when(externalLoggingClient).sendLog(messageCaptor.capture());

            // when
            loggingKafkaConsumer.consumeOrderPaidEvent(ticket, testTopic, testPartition, testOffset);

            // then
            OrderKafkaMessage capturedMessage = messageCaptor.getValue();
            assertThat(capturedMessage.getOrderItems()).hasSize(2);
            assertThat(capturedMessage.getOrderItems().get(0).getProductId()).isEqualTo(1);
            assertThat(capturedMessage.getOrderItems().get(0).getOrderQuantity()).isEqualTo(2);
            assertThat(capturedMessage.getOrderItems().get(1).getProductId()).isEqualTo(2);
            assertThat(capturedMessage.getOrderItems().get(1).getOrderQuantity()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("메시지 소비 실패 테스트")
    class ConsumeFailureTest {

        @Test
        @DisplayName("외부 로깅 시스템 호출 실패 시 예외가 발생한다")
        void consumeOrderPaidEvent_ShouldThrowException_WhenExternalLoggingFails() {
            // given
            RuntimeException exception = new RuntimeException("External logging system unavailable");
            doThrow(exception).when(externalLoggingClient).sendLog(any(OrderKafkaMessage.class));

            // when & then
            assertThatThrownBy(() ->
                    loggingKafkaConsumer.consumeOrderPaidEvent(ticket, testTopic, testPartition, testOffset)
            ).isInstanceOf(RuntimeException.class)
                    .hasMessage("External logging system unavailable");

            verify(externalLoggingClient).sendLog(any(OrderKafkaMessage.class));
        }

        @Test
        @DisplayName("네트워크 오류 발생 시 예외가 발생한다")
        void consumeOrderPaidEvent_ShouldThrowException_WhenNetworkError() {
            // given
            RuntimeException exception = new RuntimeException("Connection timeout");
            doThrow(exception).when(externalLoggingClient).sendLog(any(OrderKafkaMessage.class));

            // when & then
            assertThatThrownBy(() ->
                    loggingKafkaConsumer.consumeOrderPaidEvent(ticket, testTopic, testPartition, testOffset)
            ).isInstanceOf(RuntimeException.class)
                    .hasMessage("Connection timeout");
        }

        @Test
        @DisplayName("외부 시스템이 500 에러를 반환하면 예외가 발생한다")
        void consumeOrderPaidEvent_ShouldThrowException_WhenExternalSystemReturns500() {
            // given
            RuntimeException exception = new RuntimeException("500 Internal Server Error");
            doThrow(exception).when(externalLoggingClient).sendLog(any(OrderKafkaMessage.class));

            // when & then
            assertThatThrownBy(() ->
                    loggingKafkaConsumer.consumeOrderPaidEvent(ticket, testTopic, testPartition, testOffset)
            ).isInstanceOf(RuntimeException.class)
                    .hasMessage("500 Internal Server Error");
        }
    }

    @Nested
    @DisplayName("Kafka 헤더 처리 테스트")
    class KafkaHeadersTest {

        @Test
        @DisplayName("Kafka 토픽, 파티션, 오프셋 정보를 받아 처리한다")
        void consumeOrderPaidEvent_ShouldReceiveKafkaHeaders() {
            // given
            doNothing().when(externalLoggingClient).sendLog(any(OrderKafkaMessage.class));

            // when & then - 예외가 발생하지 않으면 성공
            assertThatCode(() ->
                    loggingKafkaConsumer.consumeOrderPaidEvent(ticket, testTopic, testPartition, testOffset)
            ).doesNotThrowAnyException();

            verify(externalLoggingClient).sendLog(any(OrderKafkaMessage.class));
        }
    }
}
