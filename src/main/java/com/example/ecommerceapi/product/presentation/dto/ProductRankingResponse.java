package com.example.ecommerceapi.product.presentation.dto;

import com.example.ecommerceapi.product.application.dto.SalesRankingResult;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 상품 판매 랭킹 응답 DTO (Presentation Layer)
 */
@Getter
@AllArgsConstructor
public class ProductRankingResponse {
    private Integer productId;
    private String productName;
    private Long totalSalesCount;
    private Long rank;

    /**
     * SalesRankingResult를 ProductRankingResponse로 변환
     */
    public static ProductRankingResponse from(SalesRankingResult result) {
        return new ProductRankingResponse(
                result.getProductId(),
                result.getProductName(),
                result.getTotalSalesCount(),
                result.getRank()
        );
    }

    /**
     * SalesRankingResult 리스트를 ProductRankingResponse 리스트로 변환
     */
    public static List<ProductRankingResponse> fromList(List<SalesRankingResult> results) {
        return results.stream()
                .map(ProductRankingResponse::from)
                .collect(Collectors.toList());
    }
}
