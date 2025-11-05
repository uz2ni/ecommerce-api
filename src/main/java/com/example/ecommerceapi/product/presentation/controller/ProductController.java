package com.example.ecommerceapi.product.presentation.controller;

import com.example.ecommerceapi.product.application.dto.IncrementProductViewResponseDto;
import com.example.ecommerceapi.product.application.dto.PopularProductResponseDto;
import com.example.ecommerceapi.product.application.dto.ProductResponseDto;
import com.example.ecommerceapi.product.application.dto.ProductStockResponseDto;
import com.example.ecommerceapi.product.presentation.dto.IncrementProductViewResponse;
import com.example.ecommerceapi.product.presentation.dto.PopularProductResponse;
import com.example.ecommerceapi.product.presentation.dto.ProductResponse;
import com.example.ecommerceapi.product.presentation.dto.ProductStockResponse;
import com.example.ecommerceapi.product.application.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
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
        List<ProductResponseDto> products = productService.getAllProducts();
        return ResponseEntity.ok(ProductResponse.fromList(products));
    }
    
    @Operation(summary = "상품 정보 조회", description = "상품 ID로 상품 상세 정보를 조회합니다.")
    @GetMapping("/{productId}")
    public ResponseEntity<ProductResponse> getProduct(
            @Parameter(description = "상품 ID", required = true)
            @PathVariable Integer productId) {

        ProductResponseDto product = productService.getProduct(productId);
        return ResponseEntity.ok(ProductResponse.from(product));
    }

    @Operation(summary = "상품 재고 조회", description = "상품 ID로 실시간 재고를 조회합니다.")
    @GetMapping("/{productId}/stock")
    public ResponseEntity<ProductStockResponse> getProductStock(
            @Parameter(description = "상품 ID", required = true)
            @PathVariable Integer productId) {

        ProductStockResponseDto stock = productService.getProductStock(productId);
        return ResponseEntity.ok(ProductStockResponse.from(stock));
    }

    @Operation(summary = "인기 상품 통계 조회", description = "조회수/판매량 기반 인기 상품 목록을 조회합니다.")
    @GetMapping("/popular")
    public ResponseEntity<List<PopularProductResponse>> getPopularProducts(
            @Parameter(description = "타입") @RequestParam(defaultValue = "SALES") String type,
            @Parameter(description = "최근 일수") @RequestParam(defaultValue = "3") @Min(1) Integer days,
            @Parameter(description = "개수") @RequestParam(defaultValue = "5") @Min(1) Integer limit
    ) {

        List<PopularProductResponseDto> popularProducts = productService.getPopularProducts(type, days, limit);
        return ResponseEntity.ok(PopularProductResponse.fromList(popularProducts));
    }

    @Operation(summary = "상품 조회수 증가", description = "상품의 조회수를 1 증가시킵니다.")
    @PatchMapping("/{productId}/view")
    public ResponseEntity<IncrementProductViewResponse> incrementProductViewCount(
            @Parameter(description = "상품 ID", required = true)
            @PathVariable Integer productId) {

        IncrementProductViewResponseDto viewCount = productService.incrementProductViewCount(productId);
        return ResponseEntity.ok(IncrementProductViewResponse.from(viewCount));
    }
}