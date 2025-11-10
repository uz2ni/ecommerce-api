package com.example.ecommerceapi.product.application.dto;

import com.example.ecommerceapi.product.domain.entity.Product;

import java.util.List;

public record PopularProductResult(
        Integer productId,
        String productName,
        Integer productPrice,
        Integer salesCount,
        Integer viewCount
) {
    public static PopularProductResult from(Product product) {
        return new PopularProductResult(
                product.getProductId(),
                product.getProductName(),
                product.getProductPrice(),
                null,
                product.getViewCount()
        );
    }

    public static PopularProductResult fromWithSales(Product product, Integer salesCount) {
        return new PopularProductResult(
                product.getProductId(),
                product.getProductName(),
                product.getProductPrice(),
                salesCount,
                product.getViewCount()
        );
    }

    public static List<PopularProductResult> fromList(List<Product> product) {
        return product.stream()
                .map(PopularProductResult::from)
                .toList();
    }
}