package com.example.ecommerceapi.order.infrastructure.kafka.producer;

import com.example.ecommerceapi.common.infrastructure.kafka.dto.Ticket;
import com.example.ecommerceapi.common.infrastructure.kafka.service.KafkaFallbackService;
import com.example.ecommerceapi.order.infrastructure.kafka.dto.OrderKafkaMessage;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderKafkaProducer 단위 테스트")
class OrderKafkaProducerTest {

    @Mock
    private KafkaTemplate<String, Ticket<OrderKafkaMessage>> kafkaTemplate;

    @Mock
    private KafkaFallbackService kafkaFallbackService;

    @InjectMocks
    private OrderKafkaProducer orderKafkaProducer;

    private OrderKafkaMessage orderKafkaMessage;
    private String testTopic = "order-paid-events";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(orderKafkaProducer, "orderPaidTopic", testTopic);

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
    }

    @Nested
    @DisplayName("메시지 발행 성공 테스트")
    class PublishSuccessTest {

        @Test
        @DisplayName("Kafka로 메시지를 성공적으로 발행한다")
        void publishOrderPaidMessage_ShouldSendMessageSuccessfully() {
            // given
            CompletableFuture<SendResult<String, Ticket<OrderKafkaMessage>>> future = new CompletableFuture<>();

            RecordMetadata metadata = new RecordMetadata(
                    new TopicPartition(testTopic, 0),
                    0L,
                    0L,
                    System.currentTimeMillis(),
                    0L,
                    0,
                    0
            );

            ProducerRecord<String, Ticket<OrderKafkaMessage>> producerRecord =
                    new ProducerRecord<>(testTopic, "1", new Ticket<>(testTopic, orderKafkaMessage));
            SendResult<String, Ticket<OrderKafkaMessage>> sendResult = new SendResult<>(producerRecord, metadata);

            future.complete(sendResult);

            given(kafkaTemplate.send(eq(testTopic), anyString(), any(Ticket.class)))
                    .willReturn(future);

            // when
            orderKafkaProducer.publishOrderPaidMessage(orderKafkaMessage);

            // then
            verify(kafkaTemplate).send(eq(testTopic), eq("1"), any(Ticket.class));
            verify(kafkaFallbackService, never()).saveFallbackMessage(anyString(), anyString(), any(), anyString());
        }

        @Test
        @DisplayName("메시지 발행 시 올바른 key를 사용한다")
        void publishOrderPaidMessage_ShouldUseCorrectKey() {
            // given
            CompletableFuture<SendResult<String, Ticket<OrderKafkaMessage>>> future = new CompletableFuture<>();

            RecordMetadata metadata = new RecordMetadata(
                    new TopicPartition(testTopic, 0),
                    0L,
                    0L,
                    System.currentTimeMillis(),
                    0L,
                    0,
                    0
            );

            ProducerRecord<String, Ticket<OrderKafkaMessage>> producerRecord =
                    new ProducerRecord<>(testTopic, "1", new Ticket<>(testTopic, orderKafkaMessage));
            SendResult<String, Ticket<OrderKafkaMessage>> sendResult = new SendResult<>(producerRecord, metadata);

            future.complete(sendResult);

            given(kafkaTemplate.send(eq(testTopic), anyString(), any(Ticket.class)))
                    .willReturn(future);

            // when
            orderKafkaProducer.publishOrderPaidMessage(orderKafkaMessage);

            // then
            ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
            verify(kafkaTemplate).send(eq(testTopic), keyCaptor.capture(), any(Ticket.class));
            assertThat(keyCaptor.getValue()).isEqualTo("1");
        }

        @Test
        @DisplayName("메시지를 Ticket으로 래핑하여 발행한다")
        void publishOrderPaidMessage_ShouldWrapMessageInTicket() {
            // given
            CompletableFuture<SendResult<String, Ticket<OrderKafkaMessage>>> future = new CompletableFuture<>();

            RecordMetadata metadata = new RecordMetadata(
                    new TopicPartition(testTopic, 0),
                    0L,
                    0L,
                    System.currentTimeMillis(),
                    0L,
                    0,
                    0
            );

            ProducerRecord<String, Ticket<OrderKafkaMessage>> producerRecord =
                    new ProducerRecord<>(testTopic, "1", new Ticket<>(testTopic, orderKafkaMessage));
            SendResult<String, Ticket<OrderKafkaMessage>> sendResult = new SendResult<>(producerRecord, metadata);

            future.complete(sendResult);

            given(kafkaTemplate.send(eq(testTopic), anyString(), any(Ticket.class)))
                    .willReturn(future);

            // when
            orderKafkaProducer.publishOrderPaidMessage(orderKafkaMessage);

            // then
            ArgumentCaptor<Ticket<OrderKafkaMessage>> ticketCaptor = ArgumentCaptor.forClass(Ticket.class);
            verify(kafkaTemplate).send(eq(testTopic), anyString(), ticketCaptor.capture());

            Ticket<OrderKafkaMessage> capturedTicket = ticketCaptor.getValue();
            assertThat(capturedTicket.getPayload()).isEqualTo(orderKafkaMessage);
            assertThat(capturedTicket.getEventType()).isEqualTo(testTopic);
            assertThat(capturedTicket.getMessageId()).isNotNull();
            assertThat(capturedTicket.getTimestamp()).isNotNull();
        }
    }

    @Nested
    @DisplayName("메시지 발행 실패 테스트")
    class PublishFailureTest {

        @Test
        @DisplayName("메시지 발행 실패 시 Fallback DB에 저장한다")
        void publishOrderPaidMessage_ShouldSaveFallback_WhenPublishFails() {
            // given
            CompletableFuture<SendResult<String, Ticket<OrderKafkaMessage>>> future = new CompletableFuture<>();
            RuntimeException exception = new RuntimeException("Kafka broker not available");
            future.completeExceptionally(exception);

            given(kafkaTemplate.send(eq(testTopic), anyString(), any(Ticket.class)))
                    .willReturn(future);

            // when
            orderKafkaProducer.publishOrderPaidMessage(orderKafkaMessage);

            // 비동기 처리 완료를 위한 대기
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // then
            verify(kafkaFallbackService).saveFallbackMessage(
                    eq(testTopic),
                    eq("1"),
                    any(Ticket.class),
                    eq("Kafka broker not available")
            );
        }

        @Test
        @DisplayName("네트워크 오류 발생 시 Fallback DB에 저장한다")
        void publishOrderPaidMessage_ShouldSaveFallback_WhenNetworkError() {
            // given
            CompletableFuture<SendResult<String, Ticket<OrderKafkaMessage>>> future = new CompletableFuture<>();
            RuntimeException exception = new RuntimeException("Connection refused");
            future.completeExceptionally(exception);

            given(kafkaTemplate.send(eq(testTopic), anyString(), any(Ticket.class)))
                    .willReturn(future);

            // when
            orderKafkaProducer.publishOrderPaidMessage(orderKafkaMessage);

            // 비동기 처리 완료를 위한 대기
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // then
            verify(kafkaFallbackService).saveFallbackMessage(
                    eq(testTopic),
                    eq("1"),
                    any(Ticket.class),
                    eq("Connection refused")
            );
        }

        @Test
        @DisplayName("타임아웃 발생 시 Fallback DB에 저장한다")
        void publishOrderPaidMessage_ShouldSaveFallback_WhenTimeout() {
            // given
            CompletableFuture<SendResult<String, Ticket<OrderKafkaMessage>>> future = new CompletableFuture<>();
            RuntimeException exception = new RuntimeException("Request timeout");
            future.completeExceptionally(exception);

            given(kafkaTemplate.send(eq(testTopic), anyString(), any(Ticket.class)))
                    .willReturn(future);

            // when
            orderKafkaProducer.publishOrderPaidMessage(orderKafkaMessage);

            // 비동기 처리 완료를 위한 대기
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // then
            verify(kafkaFallbackService).saveFallbackMessage(
                    eq(testTopic),
                    eq("1"),
                    any(Ticket.class),
                    eq("Request timeout")
            );
        }
    }
}
