package com.example.ecommerceapi.application.service.mockService;

import com.example.ecommerceapi.application.dto.product.PopularProductResponse;
import com.example.ecommerceapi.application.dto.product.ProductResponse;
import com.example.ecommerceapi.application.dto.product.ProductStockResponse;
import com.example.ecommerceapi.application.usecase.CartUseCase;
import com.example.ecommerceapi.application.usecase.ProductUseCase;
import com.example.ecommerceapi.infrastructure.memory.MockProductData;
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