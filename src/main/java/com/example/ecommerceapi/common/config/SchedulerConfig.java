package com.example.ecommerceapi.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 스케줄러 설정 클래스입니다.
 * 캐시 갱신, 조회수 동기화 등에 사용됩니다.
 */
@Configuration
@EnableScheduling
public class SchedulerConfig {
}