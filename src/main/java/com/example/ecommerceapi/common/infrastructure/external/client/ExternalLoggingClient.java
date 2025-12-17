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
     * @throws Exception 외부 시스템 호출 실패 시
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
            log.error("Failed to send log to external system: url={}, error={}",
                    loggingUrl, e.getMessage(), e);
            // 예외를 상위로 전파하여 Consumer에서 재시도나 DLQ 처리 가능하도록 함
            throw e;
        }
    }
}
