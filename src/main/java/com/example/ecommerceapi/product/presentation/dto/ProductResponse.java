package com.example.ecommerceapi.product.presentation.dto;

import com.example.ecommerceapi.product.application.dto.ProductResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    private Integer productId;
    private String productName;
    private String description;
    private Integer productPrice;

    public static ProductResponse from(ProductResult dto) {
        return ProductResponse.builder()
                .productId(dto.getProductId())
                .productName(dto.getProductName())
                .description(dto.getDescription())
                .productPrice(dto.getProductPrice())
                .build();
    }

    public static List<ProductResponse> fromList(List<ProductResult> dtos) {
        return dtos.stream()
                .map(ProductResponse::from)
                .toList();
    }
}