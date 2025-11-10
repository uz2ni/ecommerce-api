package com.example.ecommerceapi.product.presentation.dto;

import com.example.ecommerceapi.product.application.dto.IncrementProductViewResult;

public record IncrementProductViewResponse(
        Integer viewCount
) {
    public static IncrementProductViewResponse from(IncrementProductViewResult dto) {
        return new IncrementProductViewResponse(
                dto.viewCount()
        );
    }
}
