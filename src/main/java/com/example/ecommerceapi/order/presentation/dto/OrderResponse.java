package com.example.ecommerceapi.order.presentation.dto;

import com.example.ecommerceapi.order.application.dto.CreateOrderResult;
import com.example.ecommerceapi.order.application.dto.OrderItemResult;
import com.example.ecommerceapi.order.application.dto.OrderResult;
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
public class OrderResponse {
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
    private List<OrderItemResponse> orderItems;

    public static OrderResponse buildGetOrder(OrderResult order) {
        List<OrderItemResponse> orderItems = order.getOrderItems().stream().map(OrderItemResponse::from).toList();

        OrderResponse orderResult = OrderResponse.builder()
                .orderId(order.getOrderId())
                .userId(order.getUserId())
                .orderStatus(order.getOrderStatus())
                .totalOrderAmount(order.getTotalOrderAmount())
                .totalDiscountAmount(order.getTotalDiscountAmount())
                .usedPoint(order.getUsedPoint())
                .finalPaymentAmount(order.getFinalPaymentAmount())
                .deliveryUsername(order.getDeliveryUsername())
                .deliveryAddress(order.getDeliveryAddress())
                .couponId(order.getCouponId())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .orderItems(orderItems)
                .build();

        return orderResult;
    }
}