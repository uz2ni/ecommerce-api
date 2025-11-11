package com.example.ecommerceapi.product.infrastructure.persistence;

import com.example.ecommerceapi.product.domain.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * Product 도메인의 JPA Repository
 * 낙관적 락(@Version) 사용으로 동시성 제어
 */
public interface JpaProductRepository extends JpaRepository<Product, Integer> {

    /**
     * 조회수 기준 인기 상품 조회 (상위 N개)
     * idx_product_view_count 인덱스 사용
     */
    @Query("SELECT p FROM Product p ORDER BY p.viewCount DESC")
    List<Product> findAllOrderByViewCountDesc();
}
