package com.example.ecommerceapi.product.application.scheduler;

import com.example.ecommerceapi.product.application.dto.PopularProductResult;
import com.example.ecommerceapi.product.application.service.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PopularProductCacheScheduler 단위 테스트")
class PopularProductCacheSchedulerTest {

    @Mock
    private ProductService productService;

    @InjectMocks
    private PopularProductCacheScheduler scheduler;

    @Nested
    @DisplayName("판매량 기반 인기 상품 캐시 갱신 테스트")
    class EvictSalesBasedPopularProductsCacheTest {

        @Test
        @DisplayName("캐시 무효화 메서드가 정상 실행된다")
        void evictSalesBasedPopularProductsCache_ShouldExecute() {
            // when
            scheduler.evictSalesBasedPopularProductsCache();

            // then
            // @CacheEvict가 실제로 동작하는지 확인
            // 단위 테스트에서는 메서드 실행만 확인
            verifyNoInteractions(productService);
        }
    }

    @Nested
    @DisplayName("조회수 기반 인기 상품 캐시 갱신 테스트")
    class EvictViewBasedPopularProductsCacheTest {

        @Test
        @DisplayName("캐시 무효화 메서드가 정상 실행된다")
        void evictViewBasedPopularProductsCache_ShouldExecute() {
            // when
            scheduler.evictViewBasedPopularProductsCache();

            // then
            // @CacheEvict가 실제로 동작하는지 확인
            // 단위 테스트에서는 메서드 실행만 확인
            verifyNoInteractions(productService);
        }
    }

    @Nested
    @DisplayName("캐시 워밍업 테스트")
    class WarmUpCacheTest {

        @Test
        @DisplayName("애플리케이션 시작 시 캐시를 미리 채운다")
        void warmUpCache_ShouldPreloadCache() {
            // given
            PopularProductResult product1 = new PopularProductResult(
                    1,
                    "상품1",
                    10000,
                    100,
                    null
            );

            PopularProductResult product2 = new PopularProductResult(
                    2,
                    "상품2",
                    20000,
                    null,
                    200
            );

            given(productService.getPopularProducts("SALES", 3, 5))
                    .willReturn(Arrays.asList(product1));
            given(productService.getPopularProducts("VIEWS", 0, 5))
                    .willReturn(Arrays.asList(product2));

            // when
            scheduler.warmUpCache();

            // then
            verify(productService).getPopularProducts("SALES", 3, 5);
            verify(productService).getPopularProducts("VIEWS", 0, 5);
        }

        @Test
        @DisplayName("판매량 기준 조회 실패해도 조회수 기준 조회는 계속 진행한다")
        void warmUpCache_ShouldContinue_WhenSalesQueryFails() {
            // given
            PopularProductResult product = new PopularProductResult(
                    2,
                    "상품2",
                    20000,
                    null,
                    200
            );

            given(productService.getPopularProducts("SALES", 3, 5))
                    .willThrow(new RuntimeException("DB Error"));
            given(productService.getPopularProducts("VIEWS", 0, 5))
                    .willReturn(Arrays.asList(product));

            // when
            scheduler.warmUpCache();

            // then
            // SALES 조회는 실패했지만
            verify(productService).getPopularProducts("SALES", 3, 5);
            // VIEWS 조회는 계속 진행됨
            verify(productService).getPopularProducts("VIEWS", 0, 5);
        }

        @Test
        @DisplayName("조회수 기준 조회 실패해도 예외를 던지지 않는다")
        void warmUpCache_ShouldNotThrowException_WhenViewsQueryFails() {
            // given
            PopularProductResult product = new PopularProductResult(
                    1,
                    "상품1",
                    10000,
                    100,
                    null
            );

            given(productService.getPopularProducts("SALES", 3, 5))
                    .willReturn(Arrays.asList(product));
            given(productService.getPopularProducts("VIEWS", 0, 5))
                    .willThrow(new RuntimeException("DB Error"));

            // when
            scheduler.warmUpCache();

            // then
            // 예외가 발생해도 메서드는 정상 종료
            verify(productService).getPopularProducts("SALES", 3, 5);
            verify(productService).getPopularProducts("VIEWS", 0, 5);
        }

        @Test
        @DisplayName("빈 결과가 반환되어도 정상 처리한다")
        void warmUpCache_ShouldHandleEmptyResults() {
            // given
            given(productService.getPopularProducts("SALES", 3, 5))
                    .willReturn(List.of());
            given(productService.getPopularProducts("VIEWS", 0, 5))
                    .willReturn(List.of());

            // when
            scheduler.warmUpCache();

            // then
            verify(productService).getPopularProducts("SALES", 3, 5);
            verify(productService).getPopularProducts("VIEWS", 0, 5);
        }
    }
}
