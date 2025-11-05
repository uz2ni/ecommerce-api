package com.example.ecommerceapi.product.application.service;

import com.example.ecommerceapi.common.exception.ErrorCode;
import com.example.ecommerceapi.common.exception.ProductException;
import com.example.ecommerceapi.product.application.dto.IncrementProductViewResult;
import com.example.ecommerceapi.product.application.enums.ProductStatisticType;
import com.example.ecommerceapi.product.domain.entity.Product;
import com.example.ecommerceapi.product.application.dto.PopularProductResult;
import com.example.ecommerceapi.product.application.dto.ProductResult;
import com.example.ecommerceapi.product.application.dto.ProductStockResult;
import com.example.ecommerceapi.product.infrastructure.InMemoryProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Primary
@RequiredArgsConstructor
public class ProductService {

    private final InMemoryProductRepository productRepository;

    private Product validateAndGetProduct(Integer productId) {
        Product product = productRepository.findById(productId);
        if(product == null) {
            throw new ProductException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        return product;
    }

    public List<ProductResult> getAllProducts() {
        return productRepository.findAll().stream()
                .map(ProductResult::from)
                .collect(Collectors.toList());
    }

    public ProductResult getProduct(Integer productId) {
        Product product = validateAndGetProduct(productId);
        return ProductResult.from(product);
    }

    public ProductStockResult getProductStock(Integer productId) {
        Product product = validateAndGetProduct(productId);
        return ProductStockResult.from(product);
    }

    public List<PopularProductResult> getPopularProducts(String type, Integer days, Integer limit) {

        ProductStatisticType typeEnum = ProductStatisticType.valueOf(type);
        List<PopularProductResult> popularProducts;

        switch (typeEnum) {
            case SALES:
                popularProducts = getSalesStatistics(days, limit);
                break;
            case VIEWS:
                popularProducts = getViewStatistics(limit);
                break;
            default:
                throw new ProductException(ErrorCode.PRODUCT_NOT_VALID_STATISTIC);
        }

        return popularProducts;
    }

    private List<PopularProductResult> getSalesStatistics(int days, int limit) {
        List<Product> popularProducts = productRepository.findPopularProductsBySales(days, limit);
        return PopularProductResult.fromList(popularProducts);
    }

    private List<PopularProductResult> getViewStatistics(Integer limit) {
        List<Product> popularProducts = productRepository.findPopularProductsByView(limit);
        return PopularProductResult.fromList(popularProducts);
    }

    public IncrementProductViewResult incrementProductViewCount(Integer productId) {
        Product product = validateAndGetProduct(productId);
        product.incrementViewCount();
        return IncrementProductViewResult.from(product);
    }

}