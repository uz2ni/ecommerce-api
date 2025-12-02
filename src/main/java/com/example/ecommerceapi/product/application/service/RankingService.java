package com.example.ecommerceapi.product.application.service;

import com.example.ecommerceapi.product.application.dto.SalesRankingResult;
import com.example.ecommerceapi.product.domain.entity.Product;
import com.example.ecommerceapi.product.domain.entity.ProductSalesScore;
import com.example.ecommerceapi.product.domain.repository.ProductRepository;
import com.example.ecommerceapi.product.domain.repository.RankingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 판매 랭킹 애플리케이션 서비스
 * 도메인 저장소를 활용하여 판매 랭킹 비즈니스 로직 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RankingService {
    private final RankingRepository rankingRepository;
    private final ProductRepository productRepository;

    /**
     * 일간 판매량 증가
     *
     * @param productId 상품 ID
     * @param quantity 판매 수량
     * @param date 판매 날짜
     */
    public void incrementDailySalesCount(Integer productId, Integer quantity, LocalDate date) {
        rankingRepository.incrementDailySales(productId, quantity, date);
        log.debug("Incremented daily sales count for product {} by {} on {}", productId, quantity, date);
    }

    /**
     * 주간 판매량 증가
     *
     * @param productId 상품 ID
     * @param quantity 판매 수량
     * @param date 판매 날짜
     */
    public void incrementWeeklySalesCount(Integer productId, Integer quantity, LocalDate date) {
        rankingRepository.incrementWeeklySales(productId, quantity, date);
        log.debug("Incremented weekly sales count for product {} by {}", productId, quantity);
    }

    /**
     * 일간 판매 랭킹 조회 (상위 K개)
     *
     * @param date 조회할 날짜
     * @param limit 조회할 상품 개수
     * @return 판매 랭킹 결과 리스트
     */
    public List<SalesRankingResult> getDailySalesRankings(LocalDate date, int limit) {
        List<ProductSalesScore> scores = rankingRepository.findTopDailySales(date, limit);
        return convertToSalesRankingResults(scores);
    }

    /**
     * 주간 판매 랭킹 조회 (상위 K개)
     *
     * @param date 조회할 주가 포함된 날짜
     * @param limit 조회할 상품 개수
     * @return 판매 랭킹 결과 리스트
     */
    public List<SalesRankingResult> getWeeklySalesRankings(LocalDate date, int limit) {
        List<ProductSalesScore> scores = rankingRepository.findTopWeeklySales(date, limit);
        return convertToSalesRankingResults(scores);
    }

    /**
     * ProductSalesScore를 SalesRankingResult로 변환
     */
    private List<SalesRankingResult> convertToSalesRankingResults(List<ProductSalesScore> scores) {
        if (scores == null || scores.isEmpty()) {
            return Collections.emptyList();
        }

        // Product ID 추출
        List<Integer> productIds = scores.stream()
                .map(ProductSalesScore::productId)
                .toList();

        // Product 정보 조회
        Map<Integer, Product> productMap = productRepository.findAllById(productIds)
                .stream()
                .collect(Collectors.toMap(Product::getProductId, p -> p));

        // 결과 변환
        List<SalesRankingResult> results = new ArrayList<>();
        long rank = 1;

        for (ProductSalesScore score : scores) {
            Integer productId = score.productId();
            Long salesCount = score.salesCount();

            Product product = productMap.get(productId);
            String productName = product != null ? product.getProductName() : "Unknown Product";

            results.add(new SalesRankingResult(productId, productName, salesCount, rank++));
        }

        return results;
    }

    /**
     * 특정 날짜의 일간 판매 랭킹 데이터 삭제 (관리용)
     *
     * @param date 삭제할 날짜
     */
    public void deleteDailyRanking(LocalDate date) {
        rankingRepository.deleteDailyRanking(date);
        log.info("Deleted daily ranking for date: {}", date);
    }

    /**
     * 특정 주의 주간 판매 랭킹 데이터 삭제 (관리용)
     *
     * @param date 삭제할 주가 포함된 날짜
     */
    public void deleteWeeklyRanking(LocalDate date) {
        rankingRepository.deleteWeeklyRanking(date);
        log.info("Deleted weekly ranking for week: {}", date);
    }

    /**
     * 모든 랭킹 데이터 초기화 (테스트/관리용)
     */
    public void clearAllRankings() {
        rankingRepository.clearAllRankings();
        log.info("Cleared all ranking keys");
    }
}
