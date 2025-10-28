package com.example.ecommerceapi.service;

import com.example.ecommerceapi.dto.product.PopularProductResponse;
import com.example.ecommerceapi.dto.product.ProductResponse;
import com.example.ecommerceapi.dto.product.ProductStockResponse;
import com.example.ecommerceapi.mock.MockProductData;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

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