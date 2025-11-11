package com.example.ecommerceapi.product.application.service;

import com.example.ecommerceapi.common.exception.ErrorCode;
import com.example.ecommerceapi.common.exception.ProductException;
import com.example.ecommerceapi.order.domain.entity.Order;
import com.example.ecommerceapi.order.domain.entity.OrderItem;
import com.example.ecommerceapi.order.domain.entity.OrderStatus;
import com.example.ecommerceapi.order.domain.repository.OrderItemRepository;
import com.example.ecommerceapi.order.domain.repository.OrderRepository;
import com.example.ecommerceapi.product.application.dto.IncrementProductViewResult;
import com.example.ecommerceapi.product.application.enums.ProductStatisticType;
import com.example.ecommerceapi.product.application.validator.ProductValidator;
import com.example.ecommerceapi.product.domain.entity.Product;
import com.example.ecommerceapi.product.application.dto.PopularProductResult;
import com.example.ecommerceapi.product.application.dto.ProductResult;
import com.example.ecommerceapi.product.application.dto.ProductStockResult;
import com.example.ecommerceapi.product.domain.repository.ProductRepository;
import com.example.ecommerceapi.product.infrastructure.persistence.ProductTableUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@Primary
@RequiredArgsConstructor
public class ProductService {

    private final ProductValidator productValidator;
    private final ProductRepository productRepository;
    private final ProductTableUtils productTableUtils;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    public List<ProductResult> getAllProducts() {
        return productRepository.findAll().stream()
                .map(ProductResult::from)
                .collect(Collectors.toList());
    }

    public ProductResult getProduct(Integer productId) {
        Product product = productValidator.validateAndGetProduct(productId);
        return ProductResult.from(product);
    }

    public ProductStockResult getProductStock(Integer productId) {
        Product product = productValidator.validateAndGetProduct(productId);
        return ProductStockResult.from(product);
    }

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

        // 최근 N일 내의 결제 완료된 주문들 조회
        List<Order> recentPaidOrders = orderRepository.findAll().stream()
                .filter(order -> order.getOrderStatus() == OrderStatus.PAID)
                .filter(order -> order.getCreatedAt().isAfter(startDate))
                .toList();

        if (recentPaidOrders.isEmpty()) {
            return List.of();
        }

        // 주문 ID 목록 추출
        Set<Integer> orderIds = recentPaidOrders.stream()
                .map(Order::getOrderId)
                .collect(Collectors.toSet());

        // 해당 주문들의 주문 상품 조회 및 상품별 판매 수량 집계
        Map<Integer, Integer> salesMap = orderItemRepository.findAll().stream()
                .filter(item -> orderIds.contains(item.getOrderId()))
                .collect(Collectors.groupingBy(
                        OrderItem::getProductId,
                        Collectors.summingInt(OrderItem::getOrderQuantity)
                ));

        // 판매량 기준으로 정렬하고 상위 limit개 상품 조회
        List<Product> popularProducts = salesMap.entrySet().stream()
                .sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed())
                .limit(limit)
                .map(entry -> productRepository.findById(entry.getKey()))
                .filter(Objects::nonNull)
                .toList();

        return popularProducts.stream()
                .map(product -> PopularProductResult.fromWithSales(
                        product,
                        salesMap.getOrDefault(product.getProductId(), 0)
                ))
                .collect(Collectors.toList());
    }

    private List<PopularProductResult> getViewStatistics(Integer limit) {
        List<Product> popularProducts = productRepository.findPopularProductsByView(limit);
        return PopularProductResult.fromList(popularProducts);
    }

    public IncrementProductViewResult incrementProductViewCount(Integer productId) {
        Product product = productValidator.validateAndGetProduct(productId);
        product.incrementViewCount();
        return IncrementProductViewResult.from(product);
    }

    /**
     * 초기 상품 데이터 생성
     */
    public void init() {
        // 1. 테이블 초기화
        productTableUtils.resetProductTable();

        // 2. 샘플 데이터 삽입
        productRepository.init();
    }

}