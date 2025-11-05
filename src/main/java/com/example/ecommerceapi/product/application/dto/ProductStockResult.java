package com.example.ecommerceapi.product.application.dto;

import com.example.ecommerceapi.product.domain.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductStockResult {
    private Integer stock;

    public static ProductStockResult from(Product product) {
        return ProductStockResult.builder()
                .stock(product.getQuantity())
                .build();
    }
}