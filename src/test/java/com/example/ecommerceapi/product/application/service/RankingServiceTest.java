package com.example.ecommerceapi.product.application.service;

import com.example.ecommerceapi.product.application.dto.SalesRankingResult;
import com.example.ecommerceapi.product.domain.entity.Product;
import com.example.ecommerceapi.product.domain.repository.ProductRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class RankingServiceTest {

    @Autowired
    private RankingService rankingService;

    @Autowired
    private ProductRepository productRepository;

    @AfterEach
    void tearDown() {
        rankingService.clearAllRankings();
    }

    @Test
    @DisplayName("일간 판매 랭킹 - 판매량 증가 및 조회")
    void incrementAndGetDailySalesRankings() {
        // given
        LocalDate today = LocalDate.now();
        rankingService.incrementDailySalesCount(1, 10, today);
        rankingService.incrementDailySalesCount(2, 20, today);
        rankingService.incrementDailySalesCount(3, 5, today);

        // when
        List<SalesRankingResult> rankings = rankingService.getDailySalesRankings(today, 10);

        // then
        assertThat(rankings).hasSize(3);
        assertThat(rankings.get(0).getProductId()).isEqualTo(2); // 20개
        assertThat(rankings.get(0).getTotalSalesCount()).isEqualTo(20);
        assertThat(rankings.get(1).getProductId()).isEqualTo(1); // 10개
        assertThat(rankings.get(1).getTotalSalesCount()).isEqualTo(10);
        assertThat(rankings.get(2).getProductId()).isEqualTo(3); // 5개
        assertThat(rankings.get(2).getTotalSalesCount()).isEqualTo(5);
    }

    @Test
    @DisplayName("주간 판매 랭킹 - 판매량 증가 및 조회")
    void incrementAndGetWeeklySalesRankings() {
        // given
        LocalDate today = LocalDate.now();
        rankingService.incrementWeeklySalesCount(1, 15, today);
        rankingService.incrementWeeklySalesCount(2, 25, today);
        rankingService.incrementWeeklySalesCount(3, 8, today);

        // when
        List<SalesRankingResult> rankings = rankingService.getWeeklySalesRankings(today, 10);

        // then
        assertThat(rankings).hasSize(3);
        assertThat(rankings.get(0).getProductId()).isEqualTo(2); // 25개
        assertThat(rankings.get(0).getTotalSalesCount()).isEqualTo(25);
        assertThat(rankings.get(1).getProductId()).isEqualTo(1); // 15개
        assertThat(rankings.get(1).getTotalSalesCount()).isEqualTo(15);
        assertThat(rankings.get(2).getProductId()).isEqualTo(3); // 8개
        assertThat(rankings.get(2).getTotalSalesCount()).isEqualTo(8);
    }

    @Test
    @DisplayName("일간 판매 랭킹 - 여러 번 증가 시 합산")
    void incrementDailySalesCountMultipleTimes() {
        // given
        LocalDate today = LocalDate.now();
        rankingService.incrementDailySalesCount(1, 5, today);
        rankingService.incrementDailySalesCount(1, 3, today);
        rankingService.incrementDailySalesCount(1, 2, today);

        // when
        List<SalesRankingResult> rankings = rankingService.getDailySalesRankings(today, 10);

        // then
        assertThat(rankings).hasSize(1);
        assertThat(rankings.get(0).getProductId()).isEqualTo(1);
        assertThat(rankings.get(0).getTotalSalesCount()).isEqualTo(10); // 5+3+2
    }

    @Test
    @DisplayName("주간 판매 랭킹 - 여러 번 증가 시 합산")
    void incrementWeeklySalesCountMultipleTimes() {
        // given
        LocalDate today = LocalDate.now();
        rankingService.incrementWeeklySalesCount(1, 7, today);
        rankingService.incrementWeeklySalesCount(1, 4, today);
        rankingService.incrementWeeklySalesCount(1, 3, today);

        // when
        List<SalesRankingResult> rankings = rankingService.getWeeklySalesRankings(today, 10);

        // then
        assertThat(rankings).hasSize(1);
        assertThat(rankings.get(0).getProductId()).isEqualTo(1);
        assertThat(rankings.get(0).getTotalSalesCount()).isEqualTo(14); // 7+4+3
    }

    @Test
    @DisplayName("일간 판매 랭킹 - limit 적용")
    void getDailySalesRankingsWithLimit() {
        // given
        LocalDate today = LocalDate.now();
        rankingService.incrementDailySalesCount(1, 10, today);
        rankingService.incrementDailySalesCount(2, 20, today);
        rankingService.incrementDailySalesCount(3, 30, today);
        rankingService.incrementDailySalesCount(4, 40, today);
        rankingService.incrementDailySalesCount(5, 50, today);

        // when
        List<SalesRankingResult> rankings = rankingService.getDailySalesRankings(today, 3);

        // then
        assertThat(rankings).hasSize(3);
        assertThat(rankings.get(0).getProductId()).isEqualTo(5); // 50개
        assertThat(rankings.get(1).getProductId()).isEqualTo(4); // 40개
        assertThat(rankings.get(2).getProductId()).isEqualTo(3); // 30개
    }

    @Test
    @DisplayName("주간 판매 랭킹 - limit 적용")
    void getWeeklySalesRankingsWithLimit() {
        // given
        LocalDate today = LocalDate.now();
        rankingService.incrementWeeklySalesCount(1, 10, today);
        rankingService.incrementWeeklySalesCount(2, 20, today);
        rankingService.incrementWeeklySalesCount(3, 30, today);
        rankingService.incrementWeeklySalesCount(4, 40, today);
        rankingService.incrementWeeklySalesCount(5, 50, today);

        // when
        List<SalesRankingResult> rankings = rankingService.getWeeklySalesRankings(today, 3);

        // then
        assertThat(rankings).hasSize(3);
        assertThat(rankings.get(0).getProductId()).isEqualTo(5); // 50개
        assertThat(rankings.get(1).getProductId()).isEqualTo(4); // 40개
        assertThat(rankings.get(2).getProductId()).isEqualTo(3); // 30개
    }

    @Test
    @DisplayName("일간 판매 랭킹 - 서로 다른 날짜는 독립적")
    void dailySalesRankingsAreSeparatedByDate() {
        // given
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        rankingService.incrementDailySalesCount(1, 10, today);
        rankingService.incrementDailySalesCount(2, 20, yesterday);

        // when
        List<SalesRankingResult> todayRankings = rankingService.getDailySalesRankings(today, 10);
        List<SalesRankingResult> yesterdayRankings = rankingService.getDailySalesRankings(yesterday, 10);

        // then
        assertThat(todayRankings).hasSize(1);
        assertThat(todayRankings.get(0).getProductId()).isEqualTo(1);

        assertThat(yesterdayRankings).hasSize(1);
        assertThat(yesterdayRankings.get(0).getProductId()).isEqualTo(2);
    }

    @Test
    @DisplayName("일간 판매 랭킹 삭제")
    void deleteDailyRanking() {
        // given
        LocalDate today = LocalDate.now();
        rankingService.incrementDailySalesCount(1, 10, today);
        rankingService.incrementDailySalesCount(2, 20, today);

        // when
        rankingService.deleteDailyRanking(today);
        List<SalesRankingResult> rankings = rankingService.getDailySalesRankings(today, 10);

        // then
        assertThat(rankings).isEmpty();
    }

    @Test
    @DisplayName("주간 판매 랭킹 삭제")
    void deleteWeeklyRanking() {
        // given
        LocalDate today = LocalDate.now();
        rankingService.incrementWeeklySalesCount(1, 10, today);
        rankingService.incrementWeeklySalesCount(2, 20, today);

        // when
        rankingService.deleteWeeklyRanking(today);
        List<SalesRankingResult> rankings = rankingService.getWeeklySalesRankings(today, 10);

        // then
        assertThat(rankings).isEmpty();
    }

    @Test
    @DisplayName("모든 랭킹 데이터 초기화")
    void clearAllRankings() {
        // given
        LocalDate today = LocalDate.now();
        rankingService.incrementDailySalesCount(1, 10, today);
        rankingService.incrementWeeklySalesCount(1, 10, today);

        // when
        rankingService.clearAllRankings();

        // then
        List<SalesRankingResult> dailyRankings = rankingService.getDailySalesRankings(today, 10);
        List<SalesRankingResult> weeklyRankings = rankingService.getWeeklySalesRankings(today, 10);

        assertThat(dailyRankings).isEmpty();
        assertThat(weeklyRankings).isEmpty();
    }

    @Test
    @DisplayName("일간 판매 랭킹 - 상품명 조회")
    void getDailySalesRankingsWithProductName() {
        // given
        LocalDate today = LocalDate.now();
        Product product1 = productRepository.findById(1);
        Product product2 = productRepository.findById(2);

        rankingService.incrementDailySalesCount(1, 10, today);
        rankingService.incrementDailySalesCount(2, 20, today);

        // when
        List<SalesRankingResult> rankings = rankingService.getDailySalesRankings(today, 10);

        // then
        assertThat(rankings).hasSize(2);
        assertThat(rankings.get(0).getProductName()).isEqualTo(product2.getProductName());
        assertThat(rankings.get(1).getProductName()).isEqualTo(product1.getProductName());
    }

    @Test
    @DisplayName("주간 판매 랭킹 - 상품명 조회")
    void getWeeklySalesRankingsWithProductName() {
        // given
        LocalDate today = LocalDate.now();
        Product product1 = productRepository.findById(1);
        Product product2 = productRepository.findById(2);

        rankingService.incrementWeeklySalesCount(1, 10, today);
        rankingService.incrementWeeklySalesCount(2, 20, today);

        // when
        List<SalesRankingResult> rankings = rankingService.getWeeklySalesRankings(today, 10);

        // then
        assertThat(rankings).hasSize(2);
        assertThat(rankings.get(0).getProductName()).isEqualTo(product2.getProductName());
        assertThat(rankings.get(1).getProductName()).isEqualTo(product1.getProductName());
    }
}
