package com.example.ecommerceapi.order.presentation.dto;

import com.example.ecommerceapi.order.application.dto.OrderItemResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemResponse {
    private Integer orderItemId;
    private Integer orderId;
    private Integer productId;
    private String productName;
    private String description;
    private Integer productPrice;
    private Integer orderQuantity;
    private Integer totalPrice;

    public static OrderItemResponse from(OrderItemResult orderItem) {
        return OrderItemResponse.builder()
                .orderItemId(orderItem.getOrderItemId())
                .orderId(orderItem.getOrderId())
                .productId(orderItem.getProductId())
                .productName(orderItem.getProductName())
                .description(orderItem.getDescription())
                .productPrice(orderItem.getProductPrice())
                .orderQuantity(orderItem.getOrderQuantity())
                .totalPrice(orderItem.getTotalPrice())
                .build();
    }
}