package com.example.ecommerceapi.product.presentation.controller;

import com.example.ecommerceapi.product.application.dto.SalesRankingResult;
import com.example.ecommerceapi.product.application.service.RankingService;
import com.example.ecommerceapi.product.presentation.dto.ProductRankingResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "랭킹", description = "랭킹 API")
@RestController
@RequestMapping("/api/rankings")
@RequiredArgsConstructor
public class RankingController {

    private final RankingService rankingService;

    @Operation(summary = "일간 판매 랭킹 조회", description = "특정 날짜의 판매량 기준 상위 K개 상품 조회")
    @GetMapping("/sales/daily")
    public ResponseEntity<List<ProductRankingResponse>> getDailySalesRankings(
            @Parameter(description = "조회할 날짜 (YYYY-MM-DD)", example = "2025-12-02") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Parameter(description = "조회할 상품 개수", example = "10") @RequestParam(defaultValue = "10") int limit
    ) {
        LocalDate targetDate = date != null ? date : LocalDate.now();
        List<SalesRankingResult> results = rankingService.getDailySalesRankings(targetDate, limit);
        return ResponseEntity.ok(ProductRankingResponse.fromList(results));
    }

    @Operation(summary = "주간 판매 랭킹 조회", description = "특정 주의 판매량 기준 상위 K개 상품 조회")
    @GetMapping("/sales/weekly")
    public ResponseEntity<List<ProductRankingResponse>> getWeeklySalesRankings(
            @Parameter(description = "조회할 주가 포함된 날짜 (YYYY-MM-DD)", example = "2025-12-02") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Parameter(description = "조회할 상품 개수", example = "10") @RequestParam(defaultValue = "10") int limit
    ) {
        LocalDate targetDate = date != null ? date : LocalDate.now();
        List<SalesRankingResult> results = rankingService.getWeeklySalesRankings(targetDate, limit);
        return ResponseEntity.ok(ProductRankingResponse.fromList(results));
    }
}
