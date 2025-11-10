package com.example.ecommerceapi.product.application.dto;

import com.example.ecommerceapi.product.domain.entity.Product;

public record IncrementProductViewResult(
        Integer viewCount
) {
    public static IncrementProductViewResult from(Product product) {
        return new IncrementProductViewResult(
                product.getViewCount()
        );
    }
}
