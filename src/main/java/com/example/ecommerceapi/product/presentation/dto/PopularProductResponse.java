package com.example.ecommerceapi.product.presentation.dto;

import com.example.ecommerceapi.product.application.dto.PopularProductResult;

import java.util.List;

public record PopularProductResponse(
        Integer productId,
        String productName,
        Integer productPrice,
        Integer salesCount,
        Integer viewCount
) {
    public static PopularProductResponse from(PopularProductResult dto) {
        return new PopularProductResponse(
                dto.productId(),
                dto.productName(),
                dto.productPrice(),
                null,
                dto.viewCount()
        );
    }

    public static List<PopularProductResponse> fromList(List<PopularProductResult> dtos) {
        return dtos.stream()
                .map(PopularProductResponse::from)
                .toList();
    }
}