package com.example.ecommerceapi.product.infrastructure;

import com.example.ecommerceapi.product.domain.entity.Product;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public class InMemoryProductRepository {

    private Map<Integer, Product> PRODUCTS = new HashMap<>();

    @PostConstruct
    public void init() {
        PRODUCTS.put(1, Product.builder()
                .productId(1)
                .productName("유기농 딸기")
                .description("설향 품종의 달콤한 유기농 딸기입니다. (500g)")
                .productPrice(18900)
                .quantity(50)
                .viewCount(320)
                .version(1)
                .build());

        PRODUCTS.put(2, Product.builder()
                .productId(2)
                .productName("제주 감귤")
                .description("제주 청정지역에서 자란 달콤한 노지 감귤입니다. (2kg)")
                .productPrice(12900)
                .quantity(100)
                .viewCount(285)
                .version(1)
                .build());

        PRODUCTS.put(3, Product.builder()
                .productId(3)
                .productName("티라미수 케이크")
                .description("마스카포네 치즈로 만든 정통 이탈리아 티라미수입니다. (1호)")
                .productPrice(32000)
                .quantity(30)
                .viewCount(156)
                .version(1)
                .build());

        PRODUCTS.put(4, Product.builder()
                .productId(4)
                .productName("마카롱 세트")
                .description("프리미엄 수제 마카롱 10종 세트입니다.")
                .productPrice(25000)
                .quantity(40)
                .viewCount(142)
                .version(1)
                .build());

        PRODUCTS.put(5, Product.builder()
                .productId(5)
                .productName("버터 크루아상")
                .description("프랑스산 버터로 만든 겹겹이 바삭한 크루아상입니다. (4입)")
                .productPrice(8900)
                .quantity(80)
                .viewCount(210)
                .version(1)
                .build());
    }

    public List<Product> findAll() {
        return PRODUCTS.values().stream().toList();
    }

    public Product findById(Integer productId) {
        return PRODUCTS.get(productId);
    }

    public List<Product> findPopularProductsBySales(int days, int limit) {
        // TODO: 주문/결제 로직 구현 후 작성
        return null;
    }

    public List<Product> findPopularProductsByView(int limit) {
        return PRODUCTS.values().stream()
                .sorted(Comparator.comparing(Product::getViewCount).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }
}
