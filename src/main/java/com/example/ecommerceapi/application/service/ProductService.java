package com.example.ecommerceapi.application.service;

import com.example.ecommerceapi.application.dto.product.PopularProductResponse;
import com.example.ecommerceapi.application.dto.product.ProductResponse;
import com.example.ecommerceapi.application.dto.product.ProductStockResponse;
import com.example.ecommerceapi.application.usecase.ProductUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Primary
@RequiredArgsConstructor
public class ProductService implements ProductUseCase {

    @Override
    public List<ProductResponse> getAllProducts() {
        // TODO: Implement
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public ProductResponse getProduct(Integer productId) {
        // TODO: Implement
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public ProductStockResponse getProductStock(Integer productId) {
        // TODO: Implement
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public List<PopularProductResponse> getPopularProducts() {
        // TODO: Implement
        throw new UnsupportedOperationException("Not implemented yet");
    }
}