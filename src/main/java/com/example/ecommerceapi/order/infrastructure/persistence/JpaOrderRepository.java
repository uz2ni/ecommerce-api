package com.example.ecommerceapi.order.infrastructure.persistence;

import com.example.ecommerceapi.order.domain.entity.Order;
import com.example.ecommerceapi.order.domain.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Order 도메인의 JPA Repository
 */
public interface JpaOrderRepository extends JpaRepository<Order, Integer> {

    /**
     * 사용자 ID로 주문 목록 조회
     */
    List<Order> findByUserUserId(Integer userId);

    /**
     * 중복 주문 조회
     */
    boolean existsByUserUserIdAndOrderStatus(Integer userId, OrderStatus orderStatus);
}
