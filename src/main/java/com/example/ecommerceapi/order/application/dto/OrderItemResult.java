package com.example.ecommerceapi.order.application.dto;

import com.example.ecommerceapi.order.domain.entity.OrderItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemResult {
    private Integer orderItemId;
    private Integer orderId;
    private Integer productId;
    private String productName;
    private String description;
    private Integer productPrice;
    private Integer orderQuantity;
    private Integer totalPrice;

    public static OrderItemResult from(OrderItem orderItem) {
        return OrderItemResult.builder()
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
