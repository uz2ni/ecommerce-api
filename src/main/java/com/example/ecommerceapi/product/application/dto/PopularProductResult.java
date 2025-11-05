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
public class PopularProductResult {
    private Integer productId;
    private String productName;
    private Integer productPrice;
    private Integer salesCount;
    private Integer viewCount;

    public static PopularProductResult from(Product product) {
        return PopularProductResult.builder()
                .productId(product.getProductId())
                .productName(product.getProductName())
                .productPrice(product.getProductPrice())
                .salesCount(null)
                .viewCount(product.getViewCount())
                .build();
    }

    public static List<PopularProductResult> fromList(List<Product> product) {
        return product.stream()
                .map(PopularProductResult::from)
                .toList();
    }
}