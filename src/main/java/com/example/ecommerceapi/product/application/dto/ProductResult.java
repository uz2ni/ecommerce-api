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
public class ProductResult {
    private Integer productId;
    private String productName;
    private String description;
    private Integer productPrice;

    public static ProductResult from(Product product) {
        return ProductResult.builder()
                .productId(product.getProductId())
                .productName(product.getProductName())
                .description(product.getDescription())
                .productPrice(product.getProductPrice())
                .build();
    }
}