package com.example.ecommerceapi.order.dto;

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
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<OrderItemResponse> orderItems;
}