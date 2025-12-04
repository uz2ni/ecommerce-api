package com.example.ecommerceapi.product.domain.entity;

import java.time.LocalDate;

/**
 * 상품 판매 점수 도메인 엔티티
 * Redis Sorted Set의 score를 도메인 개념으로 표현
 */
public record ProductSalesScore(
        Integer productId,
        Long salesCount,
        LocalDate date,
        PeriodType periodType
) {
    /**
     * 집계 기간 타입
     */
    public enum PeriodType {
        DAILY,   // 일간
        WEEKLY   // 주간
    }

    /**
     * 판매 점수 생성
     */
    public static ProductSalesScore of(Integer productId, Long salesCount, LocalDate date, PeriodType periodType) {
        return new ProductSalesScore(productId, salesCount, date, periodType);
    }

    /**
     * 일간 판매 점수 생성
     */
    public static ProductSalesScore daily(Integer productId, Long salesCount, LocalDate date) {
        return new ProductSalesScore(productId, salesCount, date, PeriodType.DAILY);
    }

    /**
     * 주간 판매 점수 생성
     */
    public static ProductSalesScore weekly(Integer productId, Long salesCount, LocalDate date) {
        return new ProductSalesScore(productId, salesCount, date, PeriodType.WEEKLY);
    }
}
