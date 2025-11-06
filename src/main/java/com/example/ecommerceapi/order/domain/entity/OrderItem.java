package com.example.ecommerceapi.order.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {
    private Integer orderItemId;
    private Integer orderId;
    private Integer productId;
    private String productName;
    private String description;
    private Integer productPrice;
    private Integer orderQuantity;
    private Integer totalPrice;

    /**
     * 주문 상품을 생성합니다.
     */
    public static OrderItem createOrderItem(Integer orderId,
                                             Integer productId,
                                             String productName,
                                             String description,
                                             Integer productPrice,
                                             Integer orderQuantity) {
        return OrderItem.builder()
                .orderId(orderId)
                .productId(productId)
                .productName(productName)
                .description(description)
                .productPrice(productPrice)
                .orderQuantity(orderQuantity)
                .totalPrice(productPrice * orderQuantity)
                .build();
    }
}