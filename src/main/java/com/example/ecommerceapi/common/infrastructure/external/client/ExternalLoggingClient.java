package com.example.ecommerceapi.common.infrastructure.external.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * 외부 로깅 시스템(Mock Server)과 HTTP 통신을 담당하는 클라이언트
 * RestTemplate을 사용하여 외부 시스템에 로그 데이터를 전송합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExternalLoggingClient {

    private final RestTemplate restTemplate;

    @Value("${external.logging.url}")
    private String loggingUrl;

    /**
     * 외부 로깅 시스템에 로그 데이터를 전송합니다.
     *
     * @param payload 전송할 로그 데이터 (JSON 직렬화 가능한 객체)
     */
    public void sendLog(Object payload) {
        try {
            log.debug("Sending log to external system: url={}, payload={}", loggingUrl, payload);

            restTemplate.postForEntity(
                    loggingUrl,
                    payload,
                    Void.class
            );

            log.info("Successfully sent log to external system: url={}", loggingUrl);
        } catch (Exception e) {
            // 외부 로깅 실패는 메인 비즈니스 로직에 영향을 주지 않음
            log.error("Failed to send log to external system: url={}, error={}",
                    loggingUrl, e.getMessage(), e);
        }
    }
}
