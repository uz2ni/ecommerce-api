package com.example.ecommerceapi.common.infrastructure.kafka.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor
public class Ticket<T> {

    // 메시지 고유 식별자
    private String messageId = UUID.randomUUID().toString();

    // 이벤트 타입 (토픽명)
    private String eventType;

    // 실제 페이로드 (Generic 타입으로 타입 안정성 확보)
    private T payload;

    // 메시지 생성 시각
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp = LocalDateTime.now();

    public Ticket(String eventType, T payload) {
        this.payload = payload;
        this.eventType = eventType;
    }
}