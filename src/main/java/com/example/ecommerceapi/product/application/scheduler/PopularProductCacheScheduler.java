package com.example.ecommerceapi.product.application.scheduler;

import com.example.ecommerceapi.common.config.CacheType;
import com.example.ecommerceapi.product.application.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 인기 상품 캐시를 주기적으로 갱신하는 스케줄러입니다.
 * Spring Cache의 @CacheEvict를 사용하여 캐시를 무효화하고,
 * 다음 조회 시 @Cacheable에 의해 자동으로 캐시가 갱신됩니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PopularProductCacheScheduler {

    private final ProductService productService;

    // 기본 파라미터
    private static final Integer DEFAULT_DAYS = 3;
    private static final Integer DEFAULT_LIMIT = 5;

    /**
     * 판매량 기준 인기 상품 캐시 갱신 (5분마다 실행)
     */
    @Scheduled(fixedRate = 300000) // 5분 = 300,000ms
    @CacheEvict(value = CacheType.Names.POPULAR_PRODUCTS_SALES, allEntries = true)
    public void evictSalesBasedPopularProductsCache() {
        log.info("Evicted sales-based popular products cache");
    }

    /**
     * 조회수 기준 인기 상품 캐시 갱신 (30분마다 실행)
     */
    @Scheduled(fixedRate = 1800000) // 30분 = 1,800,000ms
    @CacheEvict(value = CacheType.Names.POPULAR_PRODUCTS_VIEWS, allEntries = true)
    public void evictViewBasedPopularProductsCache() {
        log.info("Evicted view-based popular products cache");
    }

    /**
     * 애플리케이션 시작 시 캐시 워밍 (5초 후 1회 실행)
     * 캐시를 미리 채워서 첫 요청의 응답 속도를 개선합니다.
     */
    @Scheduled(initialDelay = 5000, fixedRate = Long.MAX_VALUE)
    public void warmUpCache() {
        log.info("Warming up popular products cache...");

        // 판매량 기준 조회 (캐시에 자동 저장됨)
        try {
            productService.getPopularProducts("SALES", DEFAULT_DAYS, DEFAULT_LIMIT);
            log.info("Warmed up sales-based popular products cache");
        } catch (Exception e) {
            log.error("Failed to warm up sales-based cache: {}", e.getMessage(), e);
        }

        // 조회수 기준 조회 (캐시에 자동 저장됨)
        try {
            productService.getPopularProducts("VIEWS", 0, DEFAULT_LIMIT);
            log.info("Warmed up view-based popular products cache");
        } catch (Exception e) {
            log.error("Failed to warm up view-based cache: {}", e.getMessage(), e);
        }
    }
}