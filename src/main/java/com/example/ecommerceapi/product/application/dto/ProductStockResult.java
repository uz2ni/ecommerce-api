package com.example.ecommerceapi.product.application.dto;

import com.example.ecommerceapi.product.domain.entity.Product;

public record ProductStockResult(
        Integer stock
) {
    public static ProductStockResult from(Product product) {
        return new ProductStockResult(
                product.getQuantity()
        );
    }
}