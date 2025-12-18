package com.example.ecommerceapi.common.infrastructure.kafka.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Kafka 발행 실패 시 저장하는 Fallback 메시지 엔티티
 * Kafka 장애나 일시적 오류로 메시지 발행이 실패했을 때 DB에 저장하여 유실을 방지합니다.
 */
@Entity
@Table(name = "kafka_fallback_message", indexes = {
    @Index(name = "idx_status_next_retry", columnList = "status, next_retry_at")
})
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KafkaFallbackMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * Kafka 토픽
     */
    @Column(name = "topic", nullable = false)
    private String topic;

    /**
     * 메시지 키
     */
    @Column(name = "message_key")
    private String messageKey;

    /**
     * 메시지 페이로드 (JSON)
     */
    @Column(name = "payload", nullable = false, columnDefinition = "TEXT")
    private String payload;

    /**
     * 재시도 횟수
     */
    @Column(name = "retry_count", nullable = false)
    @Builder.Default
    private Integer retryCount = 0;

    /**
     * 최대 재시도 횟수
     */
    @Column(name = "max_retry", nullable = false)
    @Builder.Default
    private Integer maxRetry = 3;

    /**
     * 메시지 상태
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private FallbackMessageStatus status = FallbackMessageStatus.PENDING;

    /**
     * 에러 메시지
     */
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    /**
     * 생성 시각
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 수정 시각
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 다음 재시도 시각
     */
    @Column(name = "next_retry_at")
    private LocalDateTime nextRetryAt;

    /**
     * 재시도 증가 및 다음 재시도 시각 설정
     * 지수 백오프 전략 사용: 1분, 2분, 4분, ...
     */
    public void incrementRetry() {
        this.retryCount++;

        if (this.retryCount >= this.maxRetry) {
            this.status = FallbackMessageStatus.FAILED;
            this.nextRetryAt = null;
        } else {
            // 지수 백오프: 2^retryCount 분
            long delayMinutes = (long) Math.pow(2, this.retryCount);
            this.nextRetryAt = LocalDateTime.now().plusMinutes(delayMinutes);
        }
    }

    /**
     * 발행 성공 처리
     */
    public void markAsPublished() {
        this.status = FallbackMessageStatus.PUBLISHED;
        this.nextRetryAt = null;
    }

    /**
     * 에러 메시지 설정
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * Fallback 메시지 상태
     */
    public enum FallbackMessageStatus {
        /**
         * 발행 대기 중
         */
        PENDING,

        /**
         * 발행 완료
         */
        PUBLISHED,

        /**
         * 발행 실패 (최대 재시도 초과)
         */
        FAILED
    }
}
