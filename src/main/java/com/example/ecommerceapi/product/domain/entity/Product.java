package com.example.ecommerceapi.product.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    private Integer productId;
    private String productName;
    private String description;
    private Integer productPrice;
    private Integer quantity;
    private Integer viewCount;
    private Integer version;

    public void incrementViewCount() {
        this.viewCount += 1;
    }
}
