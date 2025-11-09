package com.example.ecommerceapi.product.domain.repository;

import com.example.ecommerceapi.product.domain.entity.Product;

import java.util.List;
import java.util.Map;

/**
 * Product 도메인의 Repository 인터페이스
 * 구현체: InMemoryProductRepository (추후 JpaProductRepository 등으로 확장 가능)
 */
public interface ProductRepository {

    /**
     * 모든 상품 조회
     */
    List<Product> findAll();

    /**
     * ID로 상품 조회
     */
    Product findById(Integer productId);

    /**
     * 판매량 기준 인기 상품 조회
     */
    List<Product> findPopularProductsBySales(int days, int limit);

    /**
     * 조회수 기준 인기 상품 조회
     */
    List<Product> findPopularProductsByView(int limit);

    /**
     * 판매 수량 맵 조회
     */
    Map<Integer, Integer> getSalesCountMap(int days);

    /**
     * 상품 저장
     */
    void save(Product product);

    /**
     * 모든 상품 삭제 (테스트용)
     */
    void clear();

    /**
     * 초기 상품 데이터 생성 (테스트용)
     */
    void init();
}