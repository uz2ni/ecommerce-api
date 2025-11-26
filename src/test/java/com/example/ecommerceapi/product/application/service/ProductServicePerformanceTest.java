package com.example.ecommerceapi.product.application.service;

import com.example.ecommerceapi.order.domain.entity.Order;
import com.example.ecommerceapi.order.domain.entity.OrderItem;
import com.example.ecommerceapi.order.domain.entity.OrderStatus;
import com.example.ecommerceapi.order.domain.repository.OrderItemRepository;
import com.example.ecommerceapi.order.domain.repository.OrderRepository;
import com.example.ecommerceapi.product.application.dto.PopularProductResult;
import com.example.ecommerceapi.product.domain.entity.Product;
import com.example.ecommerceapi.product.domain.repository.ProductRepository;
import com.example.ecommerceapi.user.domain.entity.User;
import com.example.ecommerceapi.user.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.StopWatch;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("상품 서비스 캐싱 성능 테스트")
class ProductServicePerformanceTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CacheManager cacheManager;

    private static final int WARMUP_COUNT = 5;
    private static final int TEST_COUNT = 50;

    @BeforeEach
    void setUp() {
        // 캐시 초기화
        clearAllCaches();

        // 테스트 데이터 준비
        prepareTestData();
    }

    @Test
    @DisplayName("인기 상품 조회 - 캐시 미적용 vs 캐시 적용 성능 비교")
    void comparePerformanceWithAndWithoutCache() {
        String type = "SALES";
        Integer days = 3;
        Integer limit = 5;

        System.out.println("\n========== 캐싱 성능 테스트 시작 ==========");
        System.out.println("테스트 조건: type=" + type + ", days=" + days + ", limit=" + limit);
        System.out.println("워밍업: " + WARMUP_COUNT + "회, 측정: " + TEST_COUNT + "회\n");

        // ===== 1. 캐시 미적용 (첫 번째 호출, Cache Miss) =====
        System.out.println("1️⃣ 캐시 미적용 테스트 시작 (Cache Miss)");
        clearAllCaches();

        double cacheMissAvgTime = measureAverageExecutionTime(
                () -> productService.getPopularProducts(type, days, limit),
                WARMUP_COUNT,
                TEST_COUNT,
                true
        );

        System.out.printf("캐시 미적용 평균 실행 시간: %.2fms\n\n", cacheMissAvgTime);

        // ===== 2. 캐시 적용 (두 번째 이후 호출, Cache Hit) =====
        System.out.println("2️⃣ 캐시 적용 테스트 시작 (Cache Hit)");

        // 캐시에 데이터 적재 (첫 호출)
        productService.getPopularProducts(type, days, limit);

        double cacheHitAvgTime = measureAverageExecutionTime(
                () -> productService.getPopularProducts(type, days, limit),
                WARMUP_COUNT,
                TEST_COUNT,
                false
        );

        System.out.printf("캐시 적용 평균 실행 시간: %.2fms\n\n", cacheHitAvgTime);

        // ===== 3. 결과 분석 =====
        double performanceImprovement = ((cacheMissAvgTime - cacheHitAvgTime) / cacheMissAvgTime) * 100;
        double speedupRatio = cacheMissAvgTime / cacheHitAvgTime;

        System.out.println("========== 성능 비교 결과 ==========");
        System.out.printf("캐시 미적용: %.2fms\n", cacheMissAvgTime);
        System.out.printf("캐시 적용:   %.2fms\n", cacheHitAvgTime);
        System.out.printf("성능 개선:   %.2f%%\n", performanceImprovement);
        System.out.printf("속도 향상:   %.2f배 빠름\n", speedupRatio);
        System.out.println("====================================\n");

        // 검증: 캐시 적용 시 더 빨라야 함
        assertThat(cacheHitAvgTime).isLessThan(cacheMissAvgTime);
        assertThat(performanceImprovement).isGreaterThan(0);
    }

    @Test
    @DisplayName("인기 상품 조회 - 조회수 기준 캐싱 성능 비교")
    void comparePerformanceForViewStatistics() {
        String type = "VIEWS";
        Integer days = 3;
        Integer limit = 5;

        System.out.println("\n========== 조회수 기준 캐싱 성능 테스트 ==========");

        // Cache Miss
        clearAllCaches();
        double cacheMissTime = measureSingleExecution(
                () -> productService.getPopularProducts(type, days, limit)
        );

        // Cache Hit
        productService.getPopularProducts(type, days, limit);
        double cacheHitTime = measureSingleExecution(
                () -> productService.getPopularProducts(type, days, limit)
        );

        System.out.printf("캐시 미적용: %.2fms\n", cacheMissTime);
        System.out.printf("캐시 적용:   %.2fms\n", cacheHitTime);
        System.out.printf("속도 향상:   %.2f배\n", cacheMissTime / cacheHitTime);

        assertThat(cacheHitTime).isLessThan(cacheMissTime);
    }

    @Test
    @DisplayName("다중 호출 시 캐싱 효과 측정")
    void measureCachingEffectOnMultipleCalls() {
        String type = "SALES";
        Integer days = 3;
        Integer limit = 5;
        int callCount = 100;

        System.out.println("\n========== 다중 호출 캐싱 효과 측정 ==========");
        System.out.println("호출 횟수: " + callCount + "회\n");

        // 캐시 없이 매번 DB 조회
        clearAllCaches();
        long withoutCacheTotal = 0;
        for (int i = 0; i < callCount; i++) {
            clearAllCaches(); // 매번 캐시 초기화
            long start = System.nanoTime();
            productService.getPopularProducts(type, days, limit);
            withoutCacheTotal += (System.nanoTime() - start);
        }
        double withoutCacheAvg = withoutCacheTotal / 1_000_000.0 / callCount;

        // 캐시 사용 (첫 호출 후 캐시됨)
        clearAllCaches();
        long withCacheTotal = 0;
        for (int i = 0; i < callCount; i++) {
            long start = System.nanoTime();
            productService.getPopularProducts(type, days, limit);
            withCacheTotal += (System.nanoTime() - start);
        }
        double withCacheAvg = withCacheTotal / 1_000_000.0 / callCount;

        System.out.printf("캐시 없이 %d회 평균: %.2fms (총 %.2fms)\n",
                callCount, withoutCacheAvg, withoutCacheTotal / 1_000_000.0);
        System.out.printf("캐시 사용 %d회 평균: %.2fms (총 %.2fms)\n",
                callCount, withCacheAvg, withCacheTotal / 1_000_000.0);
        System.out.printf("총 실행 시간 절감: %.2fms\n",
                (withoutCacheTotal - withCacheTotal) / 1_000_000.0);

        assertThat(withCacheAvg).isLessThan(withoutCacheAvg);
    }

    // ===== Helper Methods =====

    /**
     * 평균 실행 시간 측정
     */
    private double measureAverageExecutionTime(
            Runnable task,
            int warmupCount,
            int testCount,
            boolean clearCacheBetweenCalls
    ) {
        // 워밍업
        for (int i = 0; i < warmupCount; i++) {
            if (clearCacheBetweenCalls) {
                clearAllCaches();
            }
            task.run();
        }

        // 실제 측정
        StopWatch stopWatch = new StopWatch();
        for (int i = 0; i < testCount; i++) {
            if (clearCacheBetweenCalls) {
                clearAllCaches();
            }
            stopWatch.start();
            task.run();
            stopWatch.stop();
        }

        return stopWatch.getTotalTimeMillis() / (double) testCount;
    }

    /**
     * 단일 실행 시간 측정
     */
    private double measureSingleExecution(Runnable task) {
        long start = System.nanoTime();
        task.run();
        long end = System.nanoTime();
        return (end - start) / 1_000_000.0;
    }

    /**
     * 모든 캐시 초기화
     */
    private void clearAllCaches() {
        cacheManager.getCacheNames().forEach(cacheName ->
            Objects.requireNonNull(cacheManager.getCache(cacheName)).clear()
        );
    }

    /**
     * 테스트 데이터 준비
     */
    private void prepareTestData() {
        // 기존 데이터 정리
        orderItemRepository.clear();
        orderRepository.clear();

        // 상품이 없으면 생성
        List<Product> products = productRepository.findAll();
        if (products.isEmpty()) {
            productService.init();
            products = productRepository.findAll();
        }

        // 사용자 생성
        User user = userRepository.findById(1);
        if (user == null) {
            user = userRepository.save(User.builder()
                    .username("테스트유저")
                    .pointBalance(1000000)
                    .build());
        }

        // 주문 데이터 생성 (인기 상품 통계용)
        for (Product product : products) {
            Order order = orderRepository.save(Order.builder()
                    .user(user)
                    .orderStatus(OrderStatus.PAID)
                    .totalOrderAmount(product.getProductPrice() * 10)
                    .totalDiscountAmount(0)
                    .usedPoint(0)
                    .finalPaymentAmount(product.getProductPrice() * 10)
                    .deliveryUsername(user.getUsername())
                    .deliveryAddress("테스트 주소")
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build());

            orderItemRepository.save(OrderItem.builder()
                    .order(order)
                    .product(product)
                    .productName(product.getProductName())
                    .description(product.getDescription())
                    .productPrice(product.getProductPrice())
                    .orderQuantity(10)
                    .totalPrice(product.getProductPrice() * 10)
                    .build());
        }

        System.out.println("테스트 데이터 준비 완료: 상품 " + products.size() + "개, 주문 " + products.size() + "개");
    }
}