package com.example.ecommerceapi.order.domain.repository;

import com.example.ecommerceapi.order.domain.entity.Order;
import com.example.ecommerceapi.order.domain.entity.OrderStatus;

import java.util.List;
import java.util.Optional;

/**
 * Order 도메인의 Repository 인터페이스
 * 구현체: InMemoryOrderRepository (추후 JpaOrderRepository 등으로 확장 가능)
 */
public interface OrderRepository {

    /**
     * 주문 저장
     */
    Order save(Order order);

    /**
     * ID로 주문 조회
     */
    Optional<Order> findById(Integer orderId);

    /**
     * 사용자 ID로 주문 목록 조회
     */
    List<Order> findByUserId(Integer userId);

    /**
     * 모든 주문 조회
     */
    List<Order> findAll();

    /**
     * ID로 주문 삭제
     */
    void deleteById(Integer orderId);

    /**
     * 모든 주문 삭제 (테스트용)
     */
    void clear();

    boolean existsByUserIdAndOrderStatus(Integer userId, OrderStatus orderStatus);
}