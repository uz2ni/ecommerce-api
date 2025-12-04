package com.example.ecommerceapi.product.domain.repository;

import com.example.ecommerceapi.product.domain.entity.ProductSalesScore;

import java.time.LocalDate;
import java.util.List;

/**
 * 랭킹 저장소 인터페이스
 * 인프라 기술(Redis 등)에 독립적인 도메인 저장소 인터페이스
 */
public interface RankingRepository {

    /**
     * 일간 판매량 증가
     *
     * @param productId 상품 ID
     * @param quantity 판매 수량
     * @param date 판매 날짜
     */
    void incrementDailySales(Integer productId, Integer quantity, LocalDate date);

    /**
     * 주간 판매량 증가
     *
     * @param productId 상품 ID
     * @param quantity 판매 수량
     * @param date 판매 날짜
     */
    void incrementWeeklySales(Integer productId, Integer quantity, LocalDate date);

    /**
     * 일간 상위 판매 점수 조회
     *
     * @param date 조회할 날짜
     * @param limit 조회할 상품 개수
     * @return 상위 판매 점수 리스트
     */
    List<ProductSalesScore> findTopDailySales(LocalDate date, int limit);

    /**
     * 주간 상위 판매 점수 조회
     *
     * @param date 조회할 주가 포함된 날짜
     * @param limit 조회할 상품 개수
     * @return 상위 판매 점수 리스트
     */
    List<ProductSalesScore> findTopWeeklySales(LocalDate date, int limit);

    /**
     * 일간 랭킹 데이터 삭제 (관리용)
     *
     * @param date 삭제할 날짜
     */
    void deleteDailyRanking(LocalDate date);

    /**
     * 주간 랭킹 데이터 삭제 (관리용)
     *
     * @param date 삭제할 주가 포함된 날짜
     */
    void deleteWeeklyRanking(LocalDate date);

    /**
     * 모든 랭킹 데이터 초기화 (테스트/관리용)
     */
    void clearAllRankings();
}
