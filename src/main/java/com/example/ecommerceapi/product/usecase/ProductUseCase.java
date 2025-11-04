package com.example.ecommerceapi.product.usecase;

import com.example.ecommerceapi.product.dto.PopularProductResponse;
import com.example.ecommerceapi.product.dto.ProductResponse;
import com.example.ecommerceapi.product.dto.ProductStockResponse;

import java.util.List;

public interface ProductUseCase {

    List<ProductResponse> getAllProducts();

    ProductResponse getProduct(Integer productId);

    ProductStockResponse getProductStock(Integer productId);

    List<PopularProductResponse> getPopularProducts();
}