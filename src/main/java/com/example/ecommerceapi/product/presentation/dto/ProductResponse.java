package com.example.ecommerceapi.product.presentation.dto;

import com.example.ecommerceapi.product.application.dto.ProductResult;

import java.util.List;

public record ProductResponse(
        Integer productId,
        String productName,
        String description,
        Integer productPrice
) {
    public static ProductResponse from(ProductResult dto) {
        return new ProductResponse(
                dto.productId(),
                dto.productName(),
                dto.description(),
                dto.productPrice()
        );
    }

    public static List<ProductResponse> fromList(List<ProductResult> dtos) {
        return dtos.stream()
                .map(ProductResponse::from)
                .toList();
    }
}