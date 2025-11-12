package com.example.ecommerceapi.order.infrastructure.persistence;

import com.example.ecommerceapi.order.domain.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * OrderItem 도메인의 JPA Repository
 */
public interface JpaOrderItemRepository extends JpaRepository<OrderItem, Integer> {

    /**
     * 주문 ID로 주문 항목 목록 조회
     */
    List<OrderItem> findByOrderOrderId(Integer orderId);
}
