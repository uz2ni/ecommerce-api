package com.example.ecommerceapi.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PopularProductResponse {
    private Integer productId;
    private String productName;
    private Integer productPrice;
    private Long salesCount;
    private Long viewCount;
}