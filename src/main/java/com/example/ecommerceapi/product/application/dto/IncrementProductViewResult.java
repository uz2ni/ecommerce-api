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
public class IncrementProductViewResult {
    private Integer viewCount;

    public static IncrementProductViewResult from(Product product) {
        return IncrementProductViewResult.builder()
                .viewCount(product.getViewCount())
                .build();
    }
}
