package com.example.ecommerceapi.common.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * RestTemplate 설정
 * 외부 HTTP 통신을 위한 RestTemplate 빈을 생성합니다.
 */
@Configuration
public class RestTemplateConfig {

    /**
     * RestTemplate 빈 생성
     * - Connection Timeout: 5초
     * - Read Timeout: 10초
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofSeconds(2))
                .setReadTimeout(Duration.ofSeconds(3))
                .build();
    }
}
