package com.example.ecommerceapi.order.infrastructure;

import com.example.ecommerceapi.order.domain.entity.OrderItem;
import com.example.ecommerceapi.order.domain.repository.OrderItemRepository;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Repository
public class InMemoryOrderItemRepository implements OrderItemRepository {

    private final Map<Integer, OrderItem> orderItems = new ConcurrentHashMap<>();
    private final AtomicInteger idGenerator = new AtomicInteger(1);

    public OrderItem save(OrderItem orderItem) {
        if (orderItem.getOrderItemId() == null) {
            orderItem.setOrderItemId(idGenerator.getAndIncrement());
        }
        orderItems.put(orderItem.getOrderItemId(), orderItem);
        return orderItem;
    }

    public Optional<OrderItem> findById(Integer orderItemId) {
        return Optional.ofNullable(orderItems.get(orderItemId));
    }

    public List<OrderItem> findByOrderId(Integer orderId) {
        return orderItems.values().stream()
                .filter(item -> item.getOrderId().equals(orderId))
                .toList();
    }

    public List<OrderItem> findAll() {
        return new ArrayList<>(orderItems.values());
    }

    public void deleteById(Integer orderItemId) {
        orderItems.remove(orderItemId);
    }

    public void clear() {
        orderItems.clear();
        idGenerator.set(1);
    }
}