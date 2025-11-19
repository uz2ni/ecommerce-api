package com.example.ecommerceapi.product.application.service;

import com.example.ecommerceapi.common.exception.ErrorCode;
import com.example.ecommerceapi.common.exception.ProductException;
import com.example.ecommerceapi.order.domain.entity.OrderStatus;
import com.example.ecommerceapi.order.domain.repository.OrderItemRepository;
import com.example.ecommerceapi.product.application.dto.*;
import com.example.ecommerceapi.product.application.enums.ProductStatisticType;
import com.example.ecommerceapi.product.application.validator.ProductValidator;
import com.example.ecommerceapi.product.domain.entity.Product;
import com.example.ecommerceapi.product.domain.repository.ProductRepository;
import com.example.ecommerceapi.product.infrastructure.persistence.ProductTableUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductValidator productValidator;
    private final ProductRepository productRepository;
    private final ProductTableUtils productTableUtils;
    private final OrderItemRepository orderItemRepository;


    @Transactional(readOnly = true)
    public List<ProductResult> getAllProducts() {
        return productRepository.findAll().stream()
                .map(ProductResult::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProductResult getProduct(Integer productId) {
        Product product = productValidator.validateAndGetProduct(productId);
        return ProductResult.from(product);
    }

    @Transactional(readOnly = true)
    public ProductStockResult getProductStock(Integer productId) {
        Product product = productValidator.validateAndGetProduct(productId);
        return ProductStockResult.from(product);
    }

    @Transactional(readOnly = true)
    public List<PopularProductResult> getPopularProducts(String type, Integer days, Integer limit) {

        ProductStatisticType typeEnum = ProductStatisticType.from(type);
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
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        PageRequest pageRequest = PageRequest.of(0, limit);
        List<PopularProductBySailsResult> popularProducts = orderItemRepository.findAllOrderByOrderQuantityDesc(OrderStatus.PAID, startDate, pageRequest);

        return PopularProductResult.fromWithSalesList(popularProducts);
    }

    private List<PopularProductResult> getViewStatistics(Integer limit) {
        List<Product> popularProducts = productRepository.findPopularProductsByView(limit);
        return PopularProductResult.fromList(popularProducts);
    }

    @Transactional
    public IncrementProductViewResult incrementProductViewCount(Integer productId) {
        Product product = productValidator.validateAndGetProduct(productId);
        product.incrementViewCount();
        return IncrementProductViewResult.from(product);
    }

    /**
     * 초기 상품 데이터 생성
     */
    @Transactional
    public void init() {
        // 1. 테이블 초기화
        productTableUtils.resetProductTable();

        // 2. 샘플 데이터 삽입
        productRepository.init();
    }

}