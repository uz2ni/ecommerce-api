package com.example.ecommerceapi.order.application.dto;

import com.example.ecommerceapi.order.domain.entity.Order;
import com.example.ecommerceapi.order.domain.entity.OrderItem;

import java.time.LocalDateTime;
import java.util.List;

public record OrderResult(
        Integer orderId,
        Integer userId,
        String orderStatus,
        Integer totalOrderAmount,
        Integer totalDiscountAmount,
        Integer usedPoint,
        Integer finalPaymentAmount,
        String deliveryUsername,
        String deliveryAddress,
        Integer couponId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<OrderItemResult> orderItems
) {
    public static OrderResult buildGetOrder(Order order, List<OrderItem> orderItems) {
        List<OrderItemResult> orderItemResults = orderItems.stream().map(OrderItemResult::from).toList();

        return new OrderResult(
                order.getOrderId(),
                order.getUserId(),
                order.getOrderStatus().name(),
                order.getTotalOrderAmount(),
                order.getTotalDiscountAmount(),
                order.getUsedPoint(),
                order.getFinalPaymentAmount(),
                order.getDeliveryUsername(),
                order.getDeliveryAddress(),
                order.getCouponId(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                orderItemResults
        );
    }
}
