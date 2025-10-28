package com.example.ecommerceapi.mock;

import com.example.ecommerceapi.dto.product.PopularProductResponse;
import com.example.ecommerceapi.dto.product.ProductResponse;
import com.example.ecommerceapi.dto.product.ProductStockResponse;

import java.util.*;

public class MockProductData {

    private static final Map<Integer, ProductResponse> PRODUCTS = new HashMap<>();
    private static final Map<Integer, Integer> PRODUCT_STOCKS = new HashMap<>();

    static {
        PRODUCTS.put(1, ProductResponse.builder()
                .productId(1)
                .productName("유기농 딸기")
                .description("설향 품종의 달콤한 유기농 딸기입니다. (500g)")
                .productPrice(18900)
                .build());

        PRODUCTS.put(2, ProductResponse.builder()
                .productId(2)
                .productName("제주 감귤")
                .description("제주 청정지역에서 자란 달콤한 노지 감귤입니다. (2kg)")
                .productPrice(12900)
                .build());

        PRODUCTS.put(3, ProductResponse.builder()
                .productId(3)
                .productName("티라미수 케이크")
                .description("마스카포네 치즈로 만든 정통 이탈리아 티라미수입니다. (1호)")
                .productPrice(32000)
                .build());

        PRODUCTS.put(4, ProductResponse.builder()
                .productId(4)
                .productName("마카롱 세트")
                .description("프리미엄 수제 마카롱 10종 세트입니다.")
                .productPrice(25000)
                .build());

        PRODUCTS.put(5, ProductResponse.builder()
                .productId(5)
                .productName("버터 크루아상")
                .description("프랑스산 버터로 만든 겹겹이 바삭한 크루아상입니다. (4입)")
                .productPrice(8900)
                .build());

        // 재고 정보
        PRODUCT_STOCKS.put(1, 50);
        PRODUCT_STOCKS.put(2, 100);
        PRODUCT_STOCKS.put(3, 30);
        PRODUCT_STOCKS.put(4, 40);
        PRODUCT_STOCKS.put(5, 80);
    }

    public static List<ProductResponse> getAllProducts() {
        return new ArrayList<>(PRODUCTS.values());
    }

    public static ProductResponse getProduct(Integer productId) {
        return PRODUCTS.get(productId);
    }

    public static ProductStockResponse getProductStock(Integer productId) {
        ProductResponse product = PRODUCTS.get(productId);
        if (product == null) {
            return null;
        }

        return ProductStockResponse.builder()
                .productId(productId)
                .productName(product.getProductName())
                .stock(PRODUCT_STOCKS.getOrDefault(productId, 0))
                .build();
    }

    public static List<PopularProductResponse> getPopularProducts() {
        return Arrays.asList(
                PopularProductResponse.builder()
                        .productId(1)
                        .productName("유기농 딸기")
                        .productPrice(18900)
                        .salesCount(320L)
                        .build(),
                PopularProductResponse.builder()
                        .productId(2)
                        .productName("제주 감귤")
                        .productPrice(12900)
                        .salesCount(285L)
                        .build(),
                PopularProductResponse.builder()
                        .productId(5)
                        .productName("버터 크루아상")
                        .productPrice(8900)
                        .salesCount(210L)
                        .build(),
                PopularProductResponse.builder()
                        .productId(3)
                        .productName("티라미수 케이크")
                        .productPrice(32000)
                        .salesCount(156L)
                        .build(),
                PopularProductResponse.builder()
                        .productId(4)
                        .productName("마카롱 세트")
                        .productPrice(25000)
                        .salesCount(142L)
                        .build()
        );
    }
}