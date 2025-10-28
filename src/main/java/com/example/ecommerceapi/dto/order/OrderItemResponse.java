package com.example.ecommerceapi.dto.order;

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
    private Integer productId;
    private String productName;
    private String description;
    private Integer productPrice;
    private Integer orderQuantity;
    private Integer totalPrice;
}