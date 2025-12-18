package com.example.ecommerceapi.common.infrastructure.kafka;

import com.example.ecommerceapi.common.AbstractIntegrationTest;
import com.example.ecommerceapi.common.infrastructure.kafka.dto.Ticket;
import com.example.ecommerceapi.common.infrastructure.kafka.entity.KafkaFallbackMessage;
import com.example.ecommerceapi.common.infrastructure.kafka.repository.KafkaFallbackMessageRepository;
import com.example.ecommerceapi.common.infrastructure.kafka.service.KafkaFallbackService;
import com.example.ecommerceapi.order.infrastructure.kafka.dto.OrderKafkaMessage;
import com.example.ecommerceapi.order.infrastructure.kafka.producer.OrderKafkaProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DirtiesContext
@TestPropertySource(properties = {
        "external.logging.url=http://localhost:9999/logs",
        "kafka.topic.common.replications=1",
        "kafka.topic.common.partitions=1"
})
@DisplayName("Kafka 통합 테스트")
class KafkaIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private OrderKafkaProducer orderKafkaProducer;

    @Autowired
    private KafkaFallbackService kafkaFallbackService;

    @Autowired
    private KafkaFallbackMessageRepository fallbackMessageRepository;

    @Value("${kafka.topic.order-paid}")
    private String orderPaidTopic;

    @BeforeEach
    void setUp() {
        fallbackMessageRepository.deleteAll();
    }

    @Test
    @DisplayName("Producer가 Kafka에 메시지를 성공적으로 발행한다")
    void producer_ShouldPublishMessageToKafka() throws InterruptedException {
        // given
        OrderKafkaMessage message = OrderKafkaMessage.builder()
                .orderId(1)
                .userId(100)
                .orderItems(List.of(
                        OrderKafkaMessage.OrderItemInfo.builder()
                                .productId(1)
                                .orderQuantity(2)
                                .build()
                ))
                .paidAt(LocalDateTime.now())
                .build();

        // when
        orderKafkaProducer.publishOrderPaidMessage(message);

        // then - 비동기 발행이 완료될 때까지 잠시 대기
        Thread.sleep(2000);

        // Fallback에 저장되지 않았으면 성공적으로 발행된 것
        List<KafkaFallbackMessage> fallbackMessages = fallbackMessageRepository.findAll();
        assertThat(fallbackMessages).isEmpty();
    }

    @Test
    @DisplayName("Fallback DB에 저장된 메시지를 조회할 수 있다")
    void fallback_ShouldSaveMessageToDatabase() throws Exception {
        // given
        String testTopic = "test-topic";
        String testKey = "test-key";
        OrderKafkaMessage message = OrderKafkaMessage.builder()
                .orderId(1)
                .userId(100)
                .orderItems(List.of())
                .paidAt(LocalDateTime.now())
                .build();
        Ticket<OrderKafkaMessage> ticket = new Ticket<>(testTopic, message);

        // when
        kafkaFallbackService.saveFallbackMessage(testTopic, testKey, ticket, "Test error");

        // then
        List<KafkaFallbackMessage> messages = fallbackMessageRepository.findAll();
        assertThat(messages).isNotEmpty();

        KafkaFallbackMessage savedMessage = messages.get(0);
        assertThat(savedMessage.getTopic()).isEqualTo(testTopic);
        assertThat(savedMessage.getMessageKey()).isEqualTo(testKey);
        assertThat(savedMessage.getStatus()).isEqualTo(KafkaFallbackMessage.FallbackMessageStatus.PENDING);
        assertThat(savedMessage.getRetryCount()).isEqualTo(0);
        assertThat(savedMessage.getMaxRetry()).isEqualTo(3);
    }

    @Test
    @DisplayName("Fallback 통계를 조회할 수 있다")
    void fallback_ShouldGetStatistics() throws Exception {
        // given - PENDING 메시지 2개 저장
        for (int i = 0; i < 2; i++) {
            OrderKafkaMessage message = OrderKafkaMessage.builder()
                    .orderId(i)
                    .userId(100)
                    .orderItems(List.of())
                    .paidAt(LocalDateTime.now())
                    .build();
            Ticket<OrderKafkaMessage> ticket = new Ticket<>("test-topic", message);
            kafkaFallbackService.saveFallbackMessage("test-topic", String.valueOf(i), ticket, "Test error");
        }

        // when
        KafkaFallbackService.FallbackMessageStats stats = kafkaFallbackService.getStats();

        // then
        assertThat(stats.pending()).isEqualTo(2);
        assertThat(stats.published()).isEqualTo(0);
        assertThat(stats.failed()).isEqualTo(0);
    }
}
