package com.example.ecommerceapi.product.infrastructure.persistence;

import com.example.ecommerceapi.product.domain.entity.Product;
import com.example.ecommerceapi.product.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * ProductRepository의 JPA 구현체
 * JpaProductRepository를 사용하여 실제 DB 연동
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class JpaProductRepositoryAdapter implements ProductRepository {

    private final JpaProductRepository jpaProductRepository;
    private final ProductTableUtils productTableUtils;

    @Override
    @Transactional(readOnly = true)
    public List<Product> findAll() {
        return jpaProductRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Product findById(Integer productId) {
        return jpaProductRepository.findById(productId).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> findAllById(List<Integer> productIds) {
        return jpaProductRepository.findAllById(productIds);
    }

    @Override
    @Transactional
    public Product findByIdWithLock(Integer productId) {
        return jpaProductRepository.findByIdWithLock(productId).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> findPopularProductsByView(int limit) {
        List<Product> products = jpaProductRepository.findAllOrderByViewCountDesc();
        return products.stream()
                .limit(limit)
                .toList();
    }

    @Override
    public void save(Product product) {
        jpaProductRepository.save(product);
    }

    @Override
    public void clear() {
        productTableUtils.resetProductTable();
    }

    @Override
    public void init() {
        // 1. 테이블 초기화 (AUTO_INCREMENT 리셋)
        productTableUtils.resetProductTable();

        // 2. 초기 상품 데이터 생성
        save(Product.builder()
                .productName("유기농 딸기")
                .description("설향 품종의 달콤한 유기농 딸기입니다. (500g)")
                .productPrice(18900)
                .quantity(50)
                .viewCount(320)
                .build());

        save(Product.builder()
                .productName("제주 감귤")
                .description("제주 청정지역에서 자란 달콤한 노지 감귤입니다. (2kg)")
                .productPrice(12900)
                .quantity(100)
                .viewCount(285)
                .build());

        save(Product.builder()
                .productName("티라미수 케이크")
                .description("마스카포네 치즈로 만든 정통 이탈리아 티라미수입니다. (1호)")
                .productPrice(32000)
                .quantity(30)
                .viewCount(156)
                .build());

        save(Product.builder()
                .productName("마카롱 세트")
                .description("프리미엄 수제 마카롱 10종 세트입니다.")
                .productPrice(25000)
                .quantity(40)
                .viewCount(142)
                .build());

        save(Product.builder()
                .productName("버터 크루아상")
                .description("프랑스산 버터로 만든 겹겹이 바삭한 크루아상입니다. (4입)")
                .productPrice(8900)
                .quantity(80)
                .viewCount(210)
                .build());

        log.info("초기 상품 데이터 생성 완료");
    }
}
