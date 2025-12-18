package com.example.ecommerceapi.common.infrastructure.kafka.service;

import com.example.ecommerceapi.common.infrastructure.kafka.dto.Ticket;
import com.example.ecommerceapi.common.infrastructure.kafka.entity.KafkaFallbackMessage;
import com.example.ecommerceapi.common.infrastructure.kafka.entity.KafkaFallbackMessage.FallbackMessageStatus;
import com.example.ecommerceapi.common.infrastructure.kafka.repository.KafkaFallbackMessageRepository;
import com.example.ecommerceapi.order.infrastructure.kafka.dto.OrderKafkaMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("KafkaFallbackService 단위 테스트")
class KafkaFallbackServiceTest {

    @Mock
    private KafkaFallbackMessageRepository fallbackMessageRepository;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private KafkaFallbackService kafkaFallbackService;

    private String testTopic = "order-paid-events";
    private String testKey = "1";
    private Ticket<OrderKafkaMessage> testTicket;
    private String testPayloadJson = "{\"orderId\":1,\"userId\":100}";
    private String testErrorMessage = "Kafka broker unavailable";

    @BeforeEach
    void setUp() {
        OrderKafkaMessage orderKafkaMessage = OrderKafkaMessage.builder()
                .orderId(1)
                .userId(100)
                .build();

        testTicket = new Ticket<>(testTopic, orderKafkaMessage);
    }

    @Nested
    @DisplayName("Fallback 메시지 저장 테스트")
    class SaveFallbackMessageTest {

        @Test
        @DisplayName("Fallback 메시지를 DB에 저장한다")
        void saveFallbackMessage_ShouldSaveToDatabase() throws Exception {
            // given
            given(objectMapper.writeValueAsString(any())).willReturn(testPayloadJson);

            ArgumentCaptor<KafkaFallbackMessage> messageCaptor = ArgumentCaptor.forClass(KafkaFallbackMessage.class);
            given(fallbackMessageRepository.save(messageCaptor.capture())).willAnswer(invocation -> invocation.getArgument(0));

            // when
            kafkaFallbackService.saveFallbackMessage(testTopic, testKey, testTicket, testErrorMessage);

            // then
            verify(fallbackMessageRepository).save(any(KafkaFallbackMessage.class));

            KafkaFallbackMessage savedMessage = messageCaptor.getValue();
            assertThat(savedMessage.getTopic()).isEqualTo(testTopic);
            assertThat(savedMessage.getMessageKey()).isEqualTo(testKey);
            assertThat(savedMessage.getPayload()).isEqualTo(testPayloadJson);
            assertThat(savedMessage.getRetryCount()).isEqualTo(0);
            assertThat(savedMessage.getMaxRetry()).isEqualTo(3);
            assertThat(savedMessage.getStatus()).isEqualTo(FallbackMessageStatus.PENDING);
            assertThat(savedMessage.getErrorMessage()).isEqualTo(testErrorMessage);
            assertThat(savedMessage.getNextRetryAt()).isNotNull();
        }

        @Test
        @DisplayName("저장 실패 시 예외를 로그만 남기고 전파하지 않는다")
        void saveFallbackMessage_ShouldNotThrowException_WhenSaveFails() throws Exception {
            // given
            given(objectMapper.writeValueAsString(any())).willReturn(testPayloadJson);
            given(fallbackMessageRepository.save(any(KafkaFallbackMessage.class)))
                    .willThrow(new RuntimeException("Database error"));

            // when & then - 예외가 발생하지 않아야 함
            assertThatCode(() ->
                    kafkaFallbackService.saveFallbackMessage(testTopic, testKey, testTicket, testErrorMessage)
            ).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("첫 재시도는 1분 후로 설정된다")
        void saveFallbackMessage_ShouldSetNextRetryAfterOneMinute() throws Exception {
            // given
            given(objectMapper.writeValueAsString(any())).willReturn(testPayloadJson);

            ArgumentCaptor<KafkaFallbackMessage> messageCaptor = ArgumentCaptor.forClass(KafkaFallbackMessage.class);
            given(fallbackMessageRepository.save(messageCaptor.capture())).willAnswer(invocation -> invocation.getArgument(0));

            LocalDateTime beforeSave = LocalDateTime.now();

            // when
            kafkaFallbackService.saveFallbackMessage(testTopic, testKey, testTicket, testErrorMessage);

            // then
            KafkaFallbackMessage savedMessage = messageCaptor.getValue();
            assertThat(savedMessage.getNextRetryAt()).isAfter(beforeSave);
            assertThat(savedMessage.getNextRetryAt()).isBefore(LocalDateTime.now().plusMinutes(2));
        }
    }

    @Nested
    @DisplayName("재시도 메시지 처리 테스트")
    class RetryPendingMessagesTest {

        @Test
        @DisplayName("PENDING 상태의 재시도 대상 메시지를 조회한다")
        void retryPendingMessages_ShouldFindRetryableMessages() {
            // given
            KafkaFallbackMessage message = KafkaFallbackMessage.builder()
                    .id(1L)
                    .topic(testTopic)
                    .messageKey(testKey)
                    .payload(testPayloadJson)
                    .retryCount(0)
                    .maxRetry(3)
                    .status(FallbackMessageStatus.PENDING)
                    .nextRetryAt(LocalDateTime.now().minusMinutes(1))
                    .build();

            given(fallbackMessageRepository.findRetryableMessages(
                    eq(FallbackMessageStatus.PENDING),
                    any(LocalDateTime.class)
            )).willReturn(Arrays.asList(message));

            CompletableFuture<Object> future = new CompletableFuture<>();
            future.completeExceptionally(new RuntimeException("Retry failed"));

            given(kafkaTemplate.send(anyString(), anyString(), any()))
                    .willReturn((CompletableFuture) future);

            try {
                given(objectMapper.readValue(anyString(), eq(Object.class))).willReturn(new Object());
            } catch (Exception e) {
                // Ignore
            }

            // when
            kafkaFallbackService.retryPendingMessages();

            // then
            verify(fallbackMessageRepository).findRetryableMessages(
                    eq(FallbackMessageStatus.PENDING),
                    any(LocalDateTime.class)
            );
        }

        @Test
        @DisplayName("재시도 대상 메시지가 없으면 아무 작업도 하지 않는다")
        void retryPendingMessages_ShouldDoNothing_WhenNoMessages() {
            // given
            given(fallbackMessageRepository.findRetryableMessages(
                    eq(FallbackMessageStatus.PENDING),
                    any(LocalDateTime.class)
            )).willReturn(Arrays.asList());

            // when
            kafkaFallbackService.retryPendingMessages();

            // then
            verify(kafkaTemplate, never()).send(anyString(), anyString(), any());
        }
    }

    @Nested
    @DisplayName("재발행 성공 처리 테스트")
    class HandleRetrySuccessTest {

        @Test
        @DisplayName("재발행 성공 시 상태를 PUBLISHED로 변경한다")
        void handleRetrySuccess_ShouldMarkAsPublished() {
            // given
            KafkaFallbackMessage message = KafkaFallbackMessage.builder()
                    .id(1L)
                    .topic(testTopic)
                    .messageKey(testKey)
                    .payload(testPayloadJson)
                    .retryCount(1)
                    .maxRetry(3)
                    .status(FallbackMessageStatus.PENDING)
                    .nextRetryAt(LocalDateTime.now().plusMinutes(2))
                    .build();

            given(fallbackMessageRepository.save(any(KafkaFallbackMessage.class))).willReturn(message);

            // when
            kafkaFallbackService.handleRetrySuccess(message);

            // then
            assertThat(message.getStatus()).isEqualTo(FallbackMessageStatus.PUBLISHED);
            assertThat(message.getNextRetryAt()).isNull();
            verify(fallbackMessageRepository).save(message);
        }
    }

    @Nested
    @DisplayName("재발행 실패 처리 테스트")
    class HandleRetryFailureTest {

        @Test
        @DisplayName("재발행 실패 시 retryCount를 증가시킨다")
        void handleRetryFailure_ShouldIncrementRetryCount() {
            // given
            KafkaFallbackMessage message = KafkaFallbackMessage.builder()
                    .id(1L)
                    .topic(testTopic)
                    .messageKey(testKey)
                    .payload(testPayloadJson)
                    .retryCount(0)
                    .maxRetry(3)
                    .status(FallbackMessageStatus.PENDING)
                    .build();

            given(fallbackMessageRepository.save(any(KafkaFallbackMessage.class))).willReturn(message);

            // when
            kafkaFallbackService.handleRetryFailure(message, testErrorMessage);

            // then
            assertThat(message.getRetryCount()).isEqualTo(1);
            assertThat(message.getErrorMessage()).isEqualTo(testErrorMessage);
            verify(fallbackMessageRepository).save(message);
        }

        @Test
        @DisplayName("최대 재시도 횟수 초과 시 상태를 FAILED로 변경한다")
        void handleRetryFailure_ShouldMarkAsFailed_WhenMaxRetryExceeded() {
            // given
            KafkaFallbackMessage message = KafkaFallbackMessage.builder()
                    .id(1L)
                    .topic(testTopic)
                    .messageKey(testKey)
                    .payload(testPayloadJson)
                    .retryCount(2)
                    .maxRetry(3)
                    .status(FallbackMessageStatus.PENDING)
                    .build();

            given(fallbackMessageRepository.save(any(KafkaFallbackMessage.class))).willReturn(message);

            // when
            kafkaFallbackService.handleRetryFailure(message, testErrorMessage);

            // then
            assertThat(message.getRetryCount()).isEqualTo(3);
            assertThat(message.getStatus()).isEqualTo(FallbackMessageStatus.FAILED);
            assertThat(message.getNextRetryAt()).isNull();
            verify(fallbackMessageRepository).save(message);
        }

        @Test
        @DisplayName("지수 백오프 전략으로 다음 재시도 시각을 설정한다")
        void handleRetryFailure_ShouldSetNextRetryWithExponentialBackoff() {
            // given
            KafkaFallbackMessage message = KafkaFallbackMessage.builder()
                    .id(1L)
                    .topic(testTopic)
                    .messageKey(testKey)
                    .payload(testPayloadJson)
                    .retryCount(1)
                    .maxRetry(3)
                    .status(FallbackMessageStatus.PENDING)
                    .build();

            given(fallbackMessageRepository.save(any(KafkaFallbackMessage.class))).willReturn(message);

            LocalDateTime beforeRetry = LocalDateTime.now();

            // when
            kafkaFallbackService.handleRetryFailure(message, testErrorMessage);

            // then
            // retryCount=1 -> 2, 다음 재시도는 2^2 = 4분 후
            assertThat(message.getRetryCount()).isEqualTo(2);
            assertThat(message.getNextRetryAt()).isAfter(beforeRetry.plusMinutes(3));
            assertThat(message.getNextRetryAt()).isBefore(LocalDateTime.now().plusMinutes(5));
        }
    }

    @Nested
    @DisplayName("통계 조회 테스트")
    class GetStatsTest {

        @Test
        @DisplayName("Fallback 메시지 통계를 조회한다")
        void getStats_ShouldReturnStatistics() {
            // given
            given(fallbackMessageRepository.countByStatus(FallbackMessageStatus.PENDING)).willReturn(5L);
            given(fallbackMessageRepository.countByStatus(FallbackMessageStatus.PUBLISHED)).willReturn(10L);
            given(fallbackMessageRepository.countByStatus(FallbackMessageStatus.FAILED)).willReturn(2L);

            // when
            KafkaFallbackService.FallbackMessageStats stats = kafkaFallbackService.getStats();

            // then
            assertThat(stats.pending()).isEqualTo(5L);
            assertThat(stats.published()).isEqualTo(10L);
            assertThat(stats.failed()).isEqualTo(2L);

            verify(fallbackMessageRepository).countByStatus(FallbackMessageStatus.PENDING);
            verify(fallbackMessageRepository).countByStatus(FallbackMessageStatus.PUBLISHED);
            verify(fallbackMessageRepository).countByStatus(FallbackMessageStatus.FAILED);
        }

        @Test
        @DisplayName("메시지가 없으면 모든 통계가 0이다")
        void getStats_ShouldReturnZero_WhenNoMessages() {
            // given
            given(fallbackMessageRepository.countByStatus(any())).willReturn(0L);

            // when
            KafkaFallbackService.FallbackMessageStats stats = kafkaFallbackService.getStats();

            // then
            assertThat(stats.pending()).isEqualTo(0L);
            assertThat(stats.published()).isEqualTo(0L);
            assertThat(stats.failed()).isEqualTo(0L);
        }
    }
}
