package com.example.ecommerceapi.product.service;

import com.example.ecommerceapi.product.dto.PopularProductResponse;
import com.example.ecommerceapi.product.dto.ProductResponse;
import com.example.ecommerceapi.product.dto.ProductStockResponse;
import com.example.ecommerceapi.product.usecase.ProductUseCase;
import com.example.ecommerceapi.product.infrastructure.MockProductData;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductMockService implements ProductUseCase {

    public List<ProductResponse> getAllProducts() {
        return MockProductData.getAllProducts();
    }

    public ProductResponse getProduct(Integer productId) {
        return MockProductData.getProduct(productId);
    }

    public ProductStockResponse getProductStock(Integer productId) {
        return MockProductData.getProductStock(productId);
    }

    public List<PopularProductResponse> getPopularProducts() {
        return MockProductData.getPopularProducts();
    }
}