package com.example.ecommerceapi.order.application.dto;

import com.example.ecommerceapi.order.domain.entity.Order;
import com.example.ecommerceapi.order.domain.entity.OrderItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResult {
    private Integer orderId;
    private Integer userId;
    private String orderStatus;
    private Integer totalOrderAmount;
    private Integer totalDiscountAmount;
    private Integer usedPoint;
    private Integer finalPaymentAmount;
    private String deliveryUsername;
    private String deliveryAddress;
    private Integer couponId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<OrderItemResult> orderItems;

    public static OrderResult buildGetOrder(Order order, List<OrderItem> orderItems) {
        List<OrderItemResult> orderItemResults = orderItems.stream().map(OrderItemResult::from).toList();

        OrderResult orderResult = OrderResult.builder()
                .orderId(order.getOrderId())
                .userId(order.getUserId())
                .orderStatus(order.getOrderStatus().name())
                .totalOrderAmount(order.getTotalOrderAmount())
                .totalDiscountAmount(order.getTotalDiscountAmount())
                .usedPoint(order.getUsedPoint())
                .finalPaymentAmount(order.getFinalPaymentAmount())
                .deliveryUsername(order.getDeliveryUsername())
                .deliveryAddress(order.getDeliveryAddress())
                .couponId(order.getCouponId())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .orderItems(orderItemResults)
                .build();

        return orderResult;
    }
}
