package com.example.ecommerceapi.product.infrastructure.persistence;

import com.example.ecommerceapi.product.domain.entity.Product;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Product 도메인의 JPA Repository
 * 비관적 락(PESSIMISTIC_WRITE) 사용으로 동시성 제어
 */
public interface JpaProductRepository extends JpaRepository<Product, Integer> {

    /**
     * 조회수 기준 인기 상품 조회 (상위 N개)
     * idx_product_view_count 인덱스 사용
     */
    @Query("SELECT p FROM Product p ORDER BY p.viewCount DESC")
    List<Product> findAllOrderByViewCountDesc();

    /**
     * ID로 상품 조회 (비관적 락 - PESSIMISTIC_WRITE)
     * 재고 차감 등 동시성 제어가 필요한 경우 사용
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.productId = :productId")
    Optional<Product> findByIdWithLock(@Param("productId") Integer productId);
}
