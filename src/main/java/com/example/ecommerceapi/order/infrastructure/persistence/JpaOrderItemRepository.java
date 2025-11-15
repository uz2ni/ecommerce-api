package com.example.ecommerceapi.order.infrastructure.persistence;

import com.example.ecommerceapi.order.domain.entity.OrderItem;
import com.example.ecommerceapi.order.domain.entity.OrderStatus;
import com.example.ecommerceapi.product.application.dto.PopularProductBySailsResult;
import com.example.ecommerceapi.product.domain.entity.Product;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * OrderItem 도메인의 JPA Repository
 */
public interface JpaOrderItemRepository extends JpaRepository<OrderItem, Integer> {

    /**
     * 주문 ID로 주문 항목 목록 조회
     */
    List<OrderItem> findByOrderOrderId(Integer orderId);


    /**
     * 판매수 기준 인기 상품 조회 (상위 N개)
     * index: idx_orders_status_createdat(order_status, created_at)
     */
    @Query("""
        SELECT p AS product,
                SUM(oi.orderQuantity) AS salesCount
        FROM OrderItem oi
        JOIN oi.order o
        JOIN oi.product p
        WHERE o.orderStatus = :status
          AND o.createdAt >= :startDate
        GROUP BY p
        ORDER BY SUM(oi.orderQuantity) DESC
        """)
    List<PopularProductBySailsResult> findAllOrderByOrderQuantityDesc(
            @Param("status") OrderStatus status,
            @Param("startDate") LocalDateTime startDate,
            Pageable pageable
    );
}
