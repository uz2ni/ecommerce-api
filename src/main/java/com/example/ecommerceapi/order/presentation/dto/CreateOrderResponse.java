package com.example.ecommerceapi.order.presentation.dto;

import com.example.ecommerceapi.order.application.dto.CreateOrderResult;
import com.example.ecommerceapi.order.domain.entity.Order;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderResponse {
    private Integer orderId;
    private Integer userId;
    private String orderStatus;
    private LocalDateTime createdAt;

    public static CreateOrderResponse from(CreateOrderResult order) {
        return CreateOrderResponse.builder()
                .orderId(order.getOrderId())
                .userId(order.getUserId())
                .orderStatus(order.getOrderStatus())
                .createdAt(order.getCreatedAt())
                .build();
    }
}