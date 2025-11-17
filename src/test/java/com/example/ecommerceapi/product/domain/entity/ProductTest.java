package com.example.ecommerceapi.product.domain.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Product 엔티티 단위 테스트")
class ProductTest {

    @Nested
    @DisplayName("조회수 증가 테스트")
    class IncrementViewCountTest {
        @Test
        @DisplayName("조회수 증가 시 viewCount가 1 증가한다")
        void incrementViewCount_ShouldIncreaseViewCountByOne() {
            // given
            Product product = Product.builder()
                    .productId(1)
                    .productName("테스트 상품")
                    .description("테스트 상품 설명")
                    .productPrice(10000)
                    .quantity(100)
                    .viewCount(50)
                    .build();

            // when
            product.incrementViewCount();

            // then
            assertThat(product.getViewCount()).isEqualTo(51);
        }

        @Test
        @DisplayName("조회수가 0일 때 증가 시 viewCount가 1이 된다")
        void incrementViewCount_ShouldBeOne_WhenInitialViewCountIsZero() {
            // given
            Product product = Product.builder()
                    .productId(1)
                    .productName("테스트 상품")
                    .description("테스트 상품 설명")
                    .productPrice(10000)
                    .quantity(100)
                    .viewCount(0)
                    .build();

            // when
            product.incrementViewCount();

            // then
            assertThat(product.getViewCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("조회수를 여러 번 증가시킬 수 있다")
        void incrementViewCount_ShouldIncreaseMultipleTimes() {
            // given
            Product product = Product.builder()
                    .productId(1)
                    .productName("테스트 상품")
                    .description("테스트 상품 설명")
                    .productPrice(10000)
                    .quantity(100)
                    .viewCount(10)
                    .build();

            // when
            product.incrementViewCount();
            product.incrementViewCount();
            product.incrementViewCount();

            // then
            assertThat(product.getViewCount()).isEqualTo(13);
        }

    }

}