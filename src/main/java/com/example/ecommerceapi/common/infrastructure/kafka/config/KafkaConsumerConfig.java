package com.example.ecommerceapi.common.infrastructure.kafka.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;
import org.springframework.util.backoff.BackOff;

/**
 * Kafka Consumer 설정
 * Consumer의 재시도 및 DLQ(Dead Letter Queue) 처리를 구성합니다.
 */
@Slf4j
@Configuration
public class KafkaConsumerConfig {

    @Value("${kafka.consumer.retry.max-attempts:3}")
    private int maxAttempts;

    @Value("${kafka.consumer.retry.backoff.initial-interval:1000}")
    private long initialInterval;

    @Value("${kafka.consumer.retry.backoff.multiplier:2.0}")
    private double multiplier;

    /**
     * Consumer Error Handler 설정
     * - 재시도: 지수 백오프 전략 사용 (1초, 2초, 4초, ...)
     * - DLQ: 최대 재시도 후 실패한 메시지를 DLQ 토픽으로 전송
     */
    @Bean
    public CommonErrorHandler errorHandler(KafkaTemplate<String, Object> kafkaTemplate) {
        // 지수 백오프 설정: 초기 1초, 배수 2, 최대 3회 재시도
        ExponentialBackOffWithMaxRetries backOff = new ExponentialBackOffWithMaxRetries(maxAttempts);
        backOff.setInitialInterval(initialInterval);
        backOff.setMultiplier(multiplier);
        backOff.setMaxInterval(10000L); // 최대 10초

        // DLQ Recoverer: 실패한 메시지를 DLQ 토픽으로 전송
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                kafkaTemplate,
                (record, ex) -> {
                    // DLQ 토픽명: 원본 토픽명.DLQ
                    String dlqTopic = record.topic() + ".DLQ";
                    log.error("Publishing failed message to DLQ - original topic: {}, DLQ topic: {}, offset: {}, error: {}",
                            record.topic(), dlqTopic, record.offset(), ex.getMessage());
                    return new org.apache.kafka.common.TopicPartition(dlqTopic, record.partition());
                }
        );

        // DefaultErrorHandler: 재시도 + DLQ 처리
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, backOff);

        // 재시도하지 않을 예외 설정 (비즈니스 예외는 재시도 불필요)
        // errorHandler.addNotRetryableExceptions(BusinessException.class);

        // 재시도 리스너
        errorHandler.setRetryListeners((record, ex, deliveryAttempt) ->
                log.warn("Retrying message - topic: {}, partition: {}, offset: {}, attempt: {}/{}, error: {}",
                        record.topic(), record.partition(), record.offset(),
                        deliveryAttempt, maxAttempts, ex.getMessage())
        );

        return errorHandler;
    }

    /**
     * Kafka Listener Container Factory 커스터마이징
     * Error Handler를 자동으로 적용합니다.
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory(
            ConsumerFactory<String, Object> consumerFactory,
            CommonErrorHandler errorHandler
    ) {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setCommonErrorHandler(errorHandler);
        return factory;
    }
}
