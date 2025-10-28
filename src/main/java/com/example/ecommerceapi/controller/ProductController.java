package com.example.ecommerceapi.controller;

import com.example.ecommerceapi.dto.product.PopularProductResponse;
import com.example.ecommerceapi.dto.product.ProductResponse;
import com.example.ecommerceapi.dto.product.ProductStockResponse;
import com.example.ecommerceapi.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "상품", description = "상품 관리 API")
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "상품 목록 조회", description = "전체 상품 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts() {

        List<ProductResponse> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }
    
    @Operation(summary = "상품 정보 조회", description = "상품 ID로 상품 상세 정보를 조회합니다.")
    @GetMapping("/{productId}")
    public ResponseEntity<ProductResponse> getProduct(
            @Parameter(description = "상품 ID", required = true)
            @PathVariable Integer productId) {

        ProductResponse product = productService.getProduct(productId);
        if (product == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(product);
    }

    @Operation(summary = "상품 재고 조회", description = "상품 ID로 실시간 재고를 조회합니다.")
    @GetMapping("/{productId}/stock")
    public ResponseEntity<ProductStockResponse> getProductStock(
            @Parameter(description = "상품 ID", required = true)
            @PathVariable Integer productId) {

        ProductStockResponse stock = productService.getProductStock(productId);
        if (stock == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(stock);
    }

    @Operation(summary = "인기 상품 통계 조회", description = "최근 3일 기준 top5 인기 상품을 조회합니다.")
    @GetMapping("/popular")
    public ResponseEntity<List<PopularProductResponse>> getPopularProducts() {

        List<PopularProductResponse> popularProducts = productService.getPopularProducts();
        return ResponseEntity.ok(popularProducts);
    }
}