package com.example.ecommerceapi.common.infrastructure.kafka.repository;

import com.example.ecommerceapi.common.infrastructure.kafka.entity.KafkaFallbackMessage;
import com.example.ecommerceapi.common.infrastructure.kafka.entity.KafkaFallbackMessage.FallbackMessageStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Kafka Fallback 메시지 Repository
 */
@Repository
public interface KafkaFallbackMessageRepository extends JpaRepository<KafkaFallbackMessage, Long> {

    /**
     * 재시도 대상 메시지 조회
     * - 상태가 PENDING
     * - 다음 재시도 시각이 현재 시각 이전이거나 null인 경우
     *
     * @param now 현재 시각
     * @return 재시도 대상 메시지 목록
     */
    @Query("SELECT m FROM KafkaFallbackMessage m " +
           "WHERE m.status = :status " +
           "AND (m.nextRetryAt IS NULL OR m.nextRetryAt <= :now) " +
           "ORDER BY m.createdAt ASC")
    List<KafkaFallbackMessage> findRetryableMessages(FallbackMessageStatus status, LocalDateTime now);

    /**
     * 상태별 메시지 개수 조회
     *
     * @param status 메시지 상태
     * @return 메시지 개수
     */
    long countByStatus(FallbackMessageStatus status);
}
