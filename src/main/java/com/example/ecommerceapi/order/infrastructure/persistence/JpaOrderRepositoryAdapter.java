package com.example.ecommerceapi.order.infrastructure.persistence;

import com.example.ecommerceapi.order.domain.entity.Order;
import com.example.ecommerceapi.order.domain.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * OrderRepository의 JPA 구현체
 * JpaOrderRepository를 사용하여 실제 DB 연동
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class JpaOrderRepositoryAdapter implements OrderRepository {

    private final JpaOrderRepository jpaOrderRepository;
    private final OrderTableUtils orderTableUtils;

    @Override
    public Order save(Order order) {
        return jpaOrderRepository.save(order);
    }

    @Override
    public Optional<Order> findById(Integer orderId) {
        return jpaOrderRepository.findById(orderId);
    }

    @Override
    public List<Order> findByUserId(Integer userId) {
        return jpaOrderRepository.findByUserUserId(userId);
    }

    @Override
    public List<Order> findAll() {
        return jpaOrderRepository.findAll();
    }

    @Override
    public void deleteById(Integer orderId) {
        jpaOrderRepository.deleteById(orderId);
    }

    @Override
    public void clear() {
        orderTableUtils.resetOrderTable();
    }
}
