package com.example.ecommerceapi.product.application.service;

import com.example.ecommerceapi.common.AbstractIntegrationTest;
import com.example.ecommerceapi.product.application.dto.ProductResult;
import com.example.ecommerceapi.product.domain.entity.Product;
import com.example.ecommerceapi.product.domain.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("ProductService 캐싱 통합 테스트")
class ProductCacheIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        // Redis의 모든 키(캐시 + 락) 초기화
        clearAllRedisKeys();
    }

    @Test
    @DisplayName("전체 상품 목록 조회 시 캐싱이 적용되어야 한다")
    void getAllProducts_ShouldBeCached() {
        // when - 첫 번째 호출
        List<ProductResult> firstCall = productService.getAllProducts();

        // when - 두 번째 호출
        List<ProductResult> secondCall = productService.getAllProducts();

        // then - 동일한 데이터가 반환되어야 함 (캐시에서 조회)
        assertThat(firstCall).hasSize(secondCall.size());
        assertThat(firstCall.size()).isGreaterThan(0);

        // 각 상품의 데이터가 동일한지 확인
        for (int i = 0; i < firstCall.size(); i++) {
            assertThat(firstCall.get(i).productId()).isEqualTo(secondCall.get(i).productId());
            assertThat(firstCall.get(i).productName()).isEqualTo(secondCall.get(i).productName());
        }
    }

    @Test
    @DisplayName("단일 상품 조회 시 캐싱이 적용되어야 한다")
    void getProduct_ShouldBeCached() {
        // given
        Product product = productRepository.findAll().stream().findFirst().orElseThrow();
        Integer productId = product.getProductId();

        // when - 첫 번째 호출
        ProductResult firstCall = productService.getProduct(productId);

        // when - 두 번째 호출
        ProductResult secondCall = productService.getProduct(productId);

        // then - 동일한 데이터가 반환되어야 함 (캐시에서 조회)
        assertThat(firstCall.productId()).isEqualTo(secondCall.productId());
        assertThat(firstCall.productName()).isEqualTo(secondCall.productName());
        assertThat(firstCall.productPrice()).isEqualTo(secondCall.productPrice());
    }

    @Test
    @DisplayName("캐시를 삭제하면 다시 DB에서 조회해야 한다")
    void clearCache_ShouldReloadFromDatabase() {
        // given
        Product product = productRepository.findAll().stream().findFirst().orElseThrow();
        Integer productId = product.getProductId();
        String cacheName = "product";

        // when - 첫 번째 호출로 캐시에 저장
        ProductResult firstCall = productService.getProduct(productId);

        // when - 캐시 삭제
        cacheManager.getCache(cacheName).clear();

        // when - 다시 조회
        ProductResult secondCall = productService.getProduct(productId);

        // then - 데이터가 올바르게 조회됨
        assertThat(secondCall.productId()).isEqualTo(productId);
        assertThat(secondCall.productName()).isEqualTo(firstCall.productName());
    }

    @Test
    @DisplayName("서로 다른 상품 ID로 조회 시 각각 캐싱되어야 한다")
    void getProduct_WithDifferentIds_ShouldCacheSeparately() {
        // given
        List<Product> products = productRepository.findAll();
        assertThat(products.size()).isGreaterThanOrEqualTo(2);
        Integer productId1 = products.get(0).getProductId();
        Integer productId2 = products.get(1).getProductId();

        // when
        ProductResult result1 = productService.getProduct(productId1);
        ProductResult result2 = productService.getProduct(productId2);

        // then - 각 상품 데이터가 올바르게 조회됨
        assertThat(result1.productId()).isEqualTo(productId1);
        assertThat(result2.productId()).isEqualTo(productId2);
        assertThat(result1.productId()).isNotEqualTo(result2.productId());
    }
}