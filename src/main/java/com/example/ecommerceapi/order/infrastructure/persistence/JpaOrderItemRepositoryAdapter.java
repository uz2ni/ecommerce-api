package com.example.ecommerceapi.order.infrastructure.persistence;

import com.example.ecommerceapi.order.domain.entity.OrderItem;
import com.example.ecommerceapi.order.domain.repository.OrderItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * OrderItemRepository의 JPA 구현체
 * JpaOrderItemRepository를 사용하여 실제 DB 연동
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class JpaOrderItemRepositoryAdapter implements OrderItemRepository {

    private final JpaOrderItemRepository jpaOrderItemRepository;
    private final OrderItemTableUtils orderItemTableUtils;

    @Override
    public OrderItem save(OrderItem orderItem) {
        return jpaOrderItemRepository.save(orderItem);
    }

    @Override
    public Optional<OrderItem> findById(Integer orderItemId) {
        return jpaOrderItemRepository.findById(orderItemId);
    }

    @Override
    public List<OrderItem> findByOrderId(Integer orderId) {
        return jpaOrderItemRepository.findByOrderOrderId(orderId);
    }

    @Override
    public List<OrderItem> findAll() {
        return jpaOrderItemRepository.findAll();
    }

    @Override
    public void deleteById(Integer orderItemId) {
        jpaOrderItemRepository.deleteById(orderItemId);
    }

    @Override
    public void clear() {
        orderItemTableUtils.resetOrderItemTable();
    }
}
