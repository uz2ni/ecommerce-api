package com.example.ecommerceapi.product.application.service;

import com.example.ecommerceapi.common.exception.ErrorCode;
import com.example.ecommerceapi.common.exception.ProductException;
import com.example.ecommerceapi.product.application.dto.IncrementProductViewResponseDto;
import com.example.ecommerceapi.product.application.enums.ProductStatisticType;
import com.example.ecommerceapi.product.domain.entity.Product;
import com.example.ecommerceapi.product.application.dto.PopularProductResponseDto;
import com.example.ecommerceapi.product.application.dto.ProductResponseDto;
import com.example.ecommerceapi.product.application.dto.ProductStockResponseDto;
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

    public List<ProductResponseDto> getAllProducts() {
        return productRepository.findAll().stream()
                .map(ProductResponseDto::from)
                .collect(Collectors.toList());
    }

    public ProductResponseDto getProduct(Integer productId) {
        Product product = validateAndGetProduct(productId);
        return ProductResponseDto.from(product);
    }

    public ProductStockResponseDto getProductStock(Integer productId) {
        Product product = validateAndGetProduct(productId);
        return ProductStockResponseDto.from(product);
    }

    public List<PopularProductResponseDto> getPopularProducts(String type, Integer days, Integer limit) {

        ProductStatisticType typeEnum = ProductStatisticType.valueOf(type);
        List<PopularProductResponseDto> popularProducts;

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

    private List<PopularProductResponseDto> getSalesStatistics(int days, int limit) {
        List<Product> popularProducts = productRepository.findPopularProductsBySales(days, limit);
        return PopularProductResponseDto.fromList(popularProducts);
    }

    private List<PopularProductResponseDto> getViewStatistics(Integer limit) {
        List<Product> popularProducts = productRepository.findPopularProductsByView(limit);
        return PopularProductResponseDto.fromList(popularProducts);
    }

    public IncrementProductViewResponseDto incrementProductViewCount(Integer productId) {
        Product product = validateAndGetProduct(productId);
        product.incrementViewCount();
        return IncrementProductViewResponseDto.from(product);
    }

}