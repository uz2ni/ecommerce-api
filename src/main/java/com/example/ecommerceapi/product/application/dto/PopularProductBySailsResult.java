package com.example.ecommerceapi.product.application.dto;

import com.example.ecommerceapi.product.domain.entity.Product;

public interface PopularProductBySailsResult {
    Product getProduct();       // Product 엔티티
    Integer getSalesCount();       // SUM(oi.orderQuantity) 값
}