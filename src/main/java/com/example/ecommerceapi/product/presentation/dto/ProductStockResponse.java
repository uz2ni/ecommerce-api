package com.example.ecommerceapi.product.presentation.dto;

import com.example.ecommerceapi.product.application.dto.ProductStockResult;

public record ProductStockResponse(
        Integer stock
) {
    public static ProductStockResponse from(ProductStockResult dto) {
        return new ProductStockResponse(
                dto.stock()
        );
    }
}