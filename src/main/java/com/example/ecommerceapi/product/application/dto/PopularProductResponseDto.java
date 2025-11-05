package com.example.ecommerceapi.product.application.dto;

import com.example.ecommerceapi.product.domain.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PopularProductResponseDto {
    private Integer productId;
    private String productName;
    private Integer productPrice;
    private Integer salesCount;
    private Integer viewCount;

    public static PopularProductResponseDto from(Product product) {
        return PopularProductResponseDto.builder()
                .productId(product.getProductId())
                .productName(product.getProductName())
                .productPrice(product.getProductPrice())
                .salesCount(null)
                .viewCount(product.getViewCount())
                .build();
    }

    public static List<PopularProductResponseDto> fromList(List<Product> product) {
        return product.stream()
                .map(PopularProductResponseDto::from)
                .toList();
    }
}