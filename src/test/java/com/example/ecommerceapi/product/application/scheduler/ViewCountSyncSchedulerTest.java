package com.example.ecommerceapi.product.application.scheduler;

import com.example.ecommerceapi.product.application.service.ProductCacheService;
import com.example.ecommerceapi.product.domain.entity.Product;
import com.example.ecommerceapi.product.domain.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ViewCountSyncScheduler 단위 테스트")
class ViewCountSyncSchedulerTest {

    @Mock
    private ProductCacheService cacheService;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ViewCountSyncScheduler scheduler;

    private Product product1;
    private Product product2;

    @BeforeEach
    void setUp() {
        product1 = Product.builder()
                .productId(1)
                .productName("상품1")
                .description("상품1 설명")
                .productPrice(10000)
                .quantity(50)
                .viewCount(100)
                .build();

        product2 = Product.builder()
                .productId(2)
                .productName("상품2")
                .description("상품2 설명")
                .productPrice(20000)
                .quantity(30)
                .viewCount(200)
                .build();
    }

    @Nested
    @DisplayName("조회수 동기화 테스트")
    class SyncViewCountsToDatabaseTest {

        @Test
        @DisplayName("Redis의 조회수를 DB에 동기화한다")
        void syncViewCountsToDatabase_ShouldSyncSuccessfully() {
            // given
            List<String> keys = Arrays.asList(
                    "product:viewcount:1",
                    "product:viewcount:2"
            );
            given(cacheService.getAllViewCountKeys()).willReturn(keys);
            given(cacheService.getViewCount(1)).willReturn(10L);
            given(cacheService.getViewCount(2)).willReturn(5L);
            given(productRepository.findById(1)).willReturn(product1);
            given(productRepository.findById(2)).willReturn(product2);

            // when
            scheduler.syncViewCountsToDatabase();

            // then
            verify(cacheService).getAllViewCountKeys();
            verify(cacheService).getViewCount(1);
            verify(cacheService).getViewCount(2);
            verify(productRepository).findById(1);
            verify(productRepository).findById(2);
            verify(productRepository).save(product1);
            verify(productRepository).save(product2);
            verify(cacheService).deleteViewCount(1);
            verify(cacheService).deleteViewCount(2);
        }

        @Test
        @DisplayName("동기화할 데이터가 없으면 아무 작업도 하지 않는다")
        void syncViewCountsToDatabase_ShouldDoNothing_WhenNoKeys() {
            // given
            given(cacheService.getAllViewCountKeys()).willReturn(Collections.emptyList());

            // when
            scheduler.syncViewCountsToDatabase();

            // then
            verify(cacheService).getAllViewCountKeys();
            verify(cacheService, never()).getViewCount(any());
            verify(productRepository, never()).findById(any());
            verify(productRepository, never()).save(any());
            verify(cacheService, never()).deleteViewCount(any());
        }

        @Test
        @DisplayName("Redis 조회수가 0이면 스킵한다")
        void syncViewCountsToDatabase_ShouldSkip_WhenViewCountIsZero() {
            // given
            List<String> keys = Arrays.asList("product:viewcount:1");
            given(cacheService.getAllViewCountKeys()).willReturn(keys);
            given(cacheService.getViewCount(1)).willReturn(0L);

            // when
            scheduler.syncViewCountsToDatabase();

            // then
            verify(cacheService).getAllViewCountKeys();
            verify(cacheService).getViewCount(1);
            verify(productRepository, never()).findById(any());
            verify(productRepository, never()).save(any());
            verify(cacheService, never()).deleteViewCount(any());
        }

        @Test
        @DisplayName("Redis 조회수가 null이면 스킵한다")
        void syncViewCountsToDatabase_ShouldSkip_WhenViewCountIsNull() {
            // given
            List<String> keys = Arrays.asList("product:viewcount:1");
            given(cacheService.getAllViewCountKeys()).willReturn(keys);
            given(cacheService.getViewCount(1)).willReturn(null);

            // when
            scheduler.syncViewCountsToDatabase();

            // then
            verify(cacheService).getAllViewCountKeys();
            verify(cacheService).getViewCount(1);
            verify(productRepository, never()).findById(any());
            verify(productRepository, never()).save(any());
            verify(cacheService, never()).deleteViewCount(any());
        }

        @Test
        @DisplayName("존재하지 않는 상품은 스킵하고 계속 진행한다")
        void syncViewCountsToDatabase_ShouldSkipAndContinue_WhenProductNotFound() {
            // given
            List<String> keys = Arrays.asList(
                    "product:viewcount:999",  // 존재하지 않는 상품
                    "product:viewcount:1"     // 존재하는 상품
            );
            given(cacheService.getAllViewCountKeys()).willReturn(keys);
            given(cacheService.getViewCount(999)).willReturn(10L);
            given(cacheService.getViewCount(1)).willReturn(5L);
            given(productRepository.findById(999)).willReturn(null);
            given(productRepository.findById(1)).willReturn(product1);

            // when
            scheduler.syncViewCountsToDatabase();

            // then
            verify(productRepository).findById(999);
            verify(productRepository).findById(1);
            verify(productRepository, never()).save(argThat(p -> p != null && p.getProductId() == 999));
            verify(productRepository).save(product1);
            verify(cacheService, never()).deleteViewCount(999);  // 실패한 것은 삭제하지 않음
            verify(cacheService).deleteViewCount(1);  // 성공한 것만 삭제
        }

        @Test
        @DisplayName("특정 상품 동기화 실패해도 다른 상품은 계속 처리한다")
        void syncViewCountsToDatabase_ShouldContinue_WhenOneProductFails() {
            // given
            List<String> keys = Arrays.asList(
                    "product:viewcount:1",
                    "product:viewcount:2"
            );
            given(cacheService.getAllViewCountKeys()).willReturn(keys);
            given(cacheService.getViewCount(1)).willReturn(10L);
            given(cacheService.getViewCount(2)).willReturn(5L);
            given(productRepository.findById(1)).willThrow(new RuntimeException("DB Error"));
            given(productRepository.findById(2)).willReturn(product2);

            // when
            scheduler.syncViewCountsToDatabase();

            // then
            verify(productRepository).findById(1);
            verify(productRepository).findById(2);
            verify(productRepository).save(product2);  // product2는 정상 처리
            verify(cacheService).deleteViewCount(2);
        }

        @Test
        @DisplayName("잘못된 키 형식은 스킵하고 계속 진행한다")
        void syncViewCountsToDatabase_ShouldSkipInvalidKeyFormat() {
            // given
            List<String> keys = Arrays.asList(
                    "invalid:key",           // 잘못된 키
                    "product:viewcount:1"    // 정상 키
            );
            given(cacheService.getAllViewCountKeys()).willReturn(keys);
            given(cacheService.getViewCount(1)).willReturn(5L);
            given(productRepository.findById(1)).willReturn(product1);

            // when
            scheduler.syncViewCountsToDatabase();

            // then
            verify(productRepository).findById(1);
            verify(productRepository).save(product1);
            verify(cacheService).deleteViewCount(1);
        }

        @Test
        @DisplayName("전체 예외 발생 시에도 로그만 남기고 종료한다")
        void syncViewCountsToDatabase_ShouldHandleGlobalException() {
            // given
            given(cacheService.getAllViewCountKeys()).willThrow(new RuntimeException("Redis Connection Error"));

            // when
            scheduler.syncViewCountsToDatabase();

            // then
            // 예외가 던져지지 않고 로그만 남김
            verify(cacheService).getAllViewCountKeys();
            verify(productRepository, never()).findById(any());
            verify(productRepository, never()).save(any());
        }
    }
}