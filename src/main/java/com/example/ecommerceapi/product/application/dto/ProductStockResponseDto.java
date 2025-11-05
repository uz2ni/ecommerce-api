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
public class ProductStockResponseDto {
    private Integer stock;

    public static ProductStockResponseDto from(Product product) {
        return ProductStockResponseDto.builder()
                .stock(product.getQuantity())
                .build();
    }
}