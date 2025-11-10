package com.example.ecommerceapi.product.application.dto;

import com.example.ecommerceapi.product.domain.entity.Product;

public record ProductResult(
        Integer productId,
        String productName,
        String description,
        Integer productPrice
) {
    public static ProductResult from(Product product) {
        return new ProductResult(
                product.getProductId(),
                product.getProductName(),
                product.getDescription(),
                product.getProductPrice()
        );
    }
}