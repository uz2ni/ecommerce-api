package com.example.ecommerceapi.product.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 판매 랭킹 조회 결과 (Application Layer DTO)
 */
@Getter
@AllArgsConstructor
public class SalesRankingResult {
    private Integer productId;
    private String productName;
    private Long totalSalesCount;
    private Long rank;
}
