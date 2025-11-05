package com.example.ecommerceapi.product.presentation.dto;

import com.example.ecommerceapi.product.application.dto.PopularProductResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PopularProductResponse {
    private Integer productId;
    private String productName;
    private Integer productPrice;
    private Integer salesCount;
    private Integer viewCount;

    public static PopularProductResponse from(PopularProductResponseDto dto) {
        return PopularProductResponse.builder()
                .productId(dto.getProductId())
                .productName(dto.getProductName())
                .productPrice(dto.getProductPrice())
                .salesCount(null)
                .viewCount(dto.getViewCount())
                .build();
    }

    public static List<PopularProductResponse> fromList(List<PopularProductResponseDto> dtos) {
        return dtos.stream()
                .map(PopularProductResponse::from)
                .toList();
    }
}