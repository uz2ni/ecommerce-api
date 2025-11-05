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
public class IncrementProductViewResponseDto {
    private Integer viewCount;

    public static IncrementProductViewResponseDto from(Product product) {
        return IncrementProductViewResponseDto.builder()
                .viewCount(product.getViewCount())
                .build();
    }
}
