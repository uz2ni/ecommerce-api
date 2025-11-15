package com.example.ecommerceapi.order.domain.repository;

import com.example.ecommerceapi.order.domain.entity.OrderItem;
import com.example.ecommerceapi.order.domain.entity.OrderStatus;
import com.example.ecommerceapi.product.application.dto.PopularProductBySailsResult;
import com.example.ecommerceapi.product.domain.entity.Product;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * OrderItem 도메인의 Repository 인터페이스
 * 구현체: InMemoryOrderItemRepository (추후 JpaOrderItemRepository 등으로 확장 가능)
 */
public interface OrderItemRepository {

    /**
     * 주문 항목 저장
     */
    OrderItem save(OrderItem orderItem);

    /**
     * ID로 주문 항목 조회
     */
    Optional<OrderItem> findById(Integer orderItemId);

    /**
     * 주문 ID로 주문 항목 목록 조회
     */
    List<OrderItem> findByOrderId(Integer orderId);

    /**
     * 모든 주문 항목 조회
     */
    List<OrderItem> findAll();

    /**
     * ID로 주문 항목 삭제
     */
    void deleteById(Integer orderItemId);

    /**
     * 모든 주문 항목 삭제 (테스트용)
     */
    void clear();


    /**
     * 판매수 기준 인기 상품 조회 (상위 N개)
     */
    List<PopularProductBySailsResult> findAllOrderByOrderQuantityDesc(
            @Param("status") OrderStatus status,
            @Param("startDate") LocalDateTime startDate,
            Pageable pageable
    );
}