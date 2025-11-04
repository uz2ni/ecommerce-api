package com.example.ecommerceapi.application.dto.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductStockResponse {
    private Integer productId;
    private String productName;
    private Integer stock;
}