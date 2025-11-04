package com.example.ecommerceapi.application.usecase;

import com.example.ecommerceapi.application.dto.product.PopularProductResponse;
import com.example.ecommerceapi.application.dto.product.ProductResponse;
import com.example.ecommerceapi.application.dto.product.ProductStockResponse;

import java.util.List;

public interface ProductUseCase {

    List<ProductResponse> getAllProducts();

    ProductResponse getProduct(Integer productId);

    ProductStockResponse getProductStock(Integer productId);

    List<PopularProductResponse> getPopularProducts();
}