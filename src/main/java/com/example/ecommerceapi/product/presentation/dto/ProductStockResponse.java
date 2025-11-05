package com.example.ecommerceapi.product.presentation.dto;

import com.example.ecommerceapi.product.application.dto.ProductStockResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductStockResponse {
    private Integer stock;

    public static ProductStockResponse from(ProductStockResponseDto dto) {
        return ProductStockResponse.builder()
                .stock(dto.getStock())
                .build();
    }
}