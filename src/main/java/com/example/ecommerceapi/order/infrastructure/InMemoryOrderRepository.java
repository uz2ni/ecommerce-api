package com.example.ecommerceapi.order.infrastructure;

import com.example.ecommerceapi.order.domain.entity.Order;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Repository
public class InMemoryOrderRepository {

    private final Map<Integer, Order> orders = new ConcurrentHashMap<>();
    private final AtomicInteger idGenerator = new AtomicInteger(1);

    public Order save(Order order) {
        if (order.getOrderId() == null) {
            order.setOrderId(idGenerator.getAndIncrement());
        }
        orders.put(order.getOrderId(), order);
        return order;
    }

    public Optional<Order> findById(Integer orderId) {
        return Optional.ofNullable(orders.get(orderId));
    }

    public List<Order> findByUserId(Integer userId) {
        return orders.values().stream()
                .filter(order -> order.getUserId().equals(userId))
                .sorted(Comparator.comparing(Order::getCreatedAt).reversed())
                .toList();
    }

    public List<Order> findAll() {
        return new ArrayList<>(orders.values());
    }

    public void deleteById(Integer orderId) {
        orders.remove(orderId);
    }

    public void clear() {
        orders.clear();
        idGenerator.set(1);
    }
}