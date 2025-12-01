package com.example.ecommerceapi.product.application.scheduler;

import com.example.ecommerceapi.product.application.service.ProductCacheService;
import com.example.ecommerceapi.product.domain.entity.Product;
import com.example.ecommerceapi.product.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Redis에 저장된 조회수를 주기적으로 DB에 동기화하는 스케줄러입니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ViewCountSyncScheduler {

    private final ProductCacheService cacheService;
    private final ProductRepository productRepository;

    /**
     * 조회수 동기화 (5분마다 실행)
     */
    @Scheduled(fixedRate = 300000) // 5분 = 300,000ms
    @Transactional
    public void syncViewCountsToDatabase() {
        try {
            log.info("Starting to sync view counts from Redis to DB...");

            List<String> viewCountKeys = cacheService.getAllViewCountKeys();

            if (viewCountKeys.isEmpty()) {
                log.info("No view count data to sync");
                return;
            }

            int syncedCount = 0;
            for (String key : viewCountKeys) {
                try {
                    // 키에서 productId 추출 (product:viewcount:{productId})
                    String productIdStr = key.split(":")[2];
                    Integer productId = Integer.parseInt(productIdStr);

                    // Redis에서 조회수 가져오기
                    Long viewCount = cacheService.getViewCount(productId);

                    if (viewCount == null || viewCount == 0) {
                        continue;
                    }

                    // DB에서 상품 조회 및 업데이트
                    Product product = productRepository.findById(productId);
                    if (product != null) {
                        // 현재 DB 조회수에 Redis 조회수 추가
                        for (int i = 0; i < viewCount; i++) {
                            product.incrementViewCount();
                        }
                        productRepository.save(product);

                        // Redis 캐시 삭제 (동기화 완료)
                        cacheService.deleteViewCount(productId);

                        syncedCount++;
                        log.debug("Synced view count for productId={}: +{}", productId, viewCount);
                    } else {
                        log.warn("Product not found for productId={}, skipping sync", productId);
                    }

                } catch (Exception e) {
                    log.error("Failed to sync view count for key={}: {}", key, e.getMessage());
                }
            }

            log.info("Successfully synced {} view counts to DB", syncedCount);

        } catch (Exception e) {
            log.error("Failed to sync view counts: {}", e.getMessage(), e);
        }
    }
}