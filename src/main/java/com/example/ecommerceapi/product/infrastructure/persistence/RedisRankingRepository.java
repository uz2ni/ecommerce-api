package com.example.ecommerceapi.product.infrastructure.persistence;

import com.example.ecommerceapi.common.redis.StorageType;
import com.example.ecommerceapi.product.domain.entity.ProductSalesScore;
import com.example.ecommerceapi.product.domain.repository.RankingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.stream.Collectors;

/**
 * Redis를 사용한 랭킹 저장소 구현체
 * Redis Sorted Set을 활용하여 판매 랭킹 데이터 관리
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class RedisRankingRepository implements RankingRepository {

    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public void incrementDailySales(Integer productId, Integer quantity, LocalDate date) {
        String key = StorageType.DAILY_SALES_RANKING.getKeyWithDate(date);

        try {
            /**
             * SortedSet productId 구매수량 증가 처리
             * ZINCRBY daily:sales:YYYY-MM-DD quantity "productId"
             * TTL은 RankingKeyInitScheduler에서 관리
             */
            redisTemplate.opsForZSet()
                    .incrementScore(key, String.valueOf(productId), quantity);

            log.debug("Incremented daily sales count for product {} by {} on {}", productId, quantity, date);
        } catch (Exception e) {
            log.error("Failed to increment daily sales count for product {}: {}", productId, e.getMessage(), e);
        }
    }

    @Override
    public void incrementWeeklySales(Integer productId, Integer quantity, LocalDate date) {
        String weekKey = getWeekKey(date);
        String key = StorageType.WEEKLY_SALES_RANKING.getKeyWithWeek(weekKey);

        try {
            /**
             * TTL은 RankingKeyInitScheduler에서 관리
             */
            redisTemplate.opsForZSet()
                    .incrementScore(key, String.valueOf(productId), quantity);

            log.debug("Incremented weekly sales count for product {} by {} for week {}", productId, quantity, weekKey);
        } catch (Exception e) {
            log.error("Failed to increment weekly sales count for product {}: {}", productId, e.getMessage(), e);
        }
    }

    @Override
    public List<ProductSalesScore> findTopDailySales(LocalDate date, int limit) {
        String key = StorageType.DAILY_SALES_RANKING.getKeyWithDate(date);

        try {
            Set<ZSetOperations.TypedTuple<String>> results =
                    redisTemplate.opsForZSet()
                            .reverseRangeWithScores(key, 0, limit - 1);

            return convertToProductSalesScores(results, date, ProductSalesScore.PeriodType.DAILY);
        } catch (Exception e) {
            log.error("Failed to get daily sales rankings: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<ProductSalesScore> findTopWeeklySales(LocalDate date, int limit) {
        String weekKey = getWeekKey(date);
        String key = StorageType.WEEKLY_SALES_RANKING.getKeyWithWeek(weekKey);

        try {
            Set<ZSetOperations.TypedTuple<String>> results =
                    redisTemplate.opsForZSet()
                            .reverseRangeWithScores(key, 0, limit - 1);

            return convertToProductSalesScores(results, date, ProductSalesScore.PeriodType.WEEKLY);
        } catch (Exception e) {
            log.error("Failed to get weekly sales rankings: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    public void deleteDailyRanking(LocalDate date) {
        String key = StorageType.DAILY_SALES_RANKING.getKeyWithDate(date);
        redisTemplate.delete(key);
        log.info("Deleted daily ranking for date: {}", date);
    }

    @Override
    public void deleteWeeklyRanking(LocalDate date) {
        String weekKey = getWeekKey(date);
        String key = StorageType.WEEKLY_SALES_RANKING.getKeyWithWeek(weekKey);
        redisTemplate.delete(key);
        log.info("Deleted weekly ranking for week: {}", weekKey);
    }

    @Override
    public void clearAllRankings() {
        Set<String> dailyKeys = redisTemplate.keys(StorageType.DAILY_SALES_RANKING.getKey() + ":*");
        Set<String> weeklyKeys = redisTemplate.keys(StorageType.WEEKLY_SALES_RANKING.getKey() + ":*");

        int deletedCount = 0;

        if (dailyKeys != null && !dailyKeys.isEmpty()) {
            redisTemplate.delete(dailyKeys);
            deletedCount += dailyKeys.size();
        }

        if (weeklyKeys != null && !weeklyKeys.isEmpty()) {
            redisTemplate.delete(weeklyKeys);
            deletedCount += weeklyKeys.size();
        }

        log.info("Cleared {} ranking keys", deletedCount);
    }

    /**
     * Redis 결과를 ProductSalesScore 도메인 엔티티로 변환
     */
    private List<ProductSalesScore> convertToProductSalesScores(
            Set<ZSetOperations.TypedTuple<String>> redisResults,
            LocalDate date,
            ProductSalesScore.PeriodType periodType) {

        if (redisResults == null || redisResults.isEmpty()) {
            return Collections.emptyList();
        }

        return redisResults.stream()
                .map(tuple -> {
                    Integer productId = Integer.valueOf(tuple.getValue());
                    Long salesCount = tuple.getScore() != null ? tuple.getScore().longValue() : 0L;
                    return ProductSalesScore.of(productId, salesCount, date, periodType);
                })
                .collect(Collectors.toList());
    }

    /**
     * 날짜로부터 주 키 생성 (예: 2025-W01)
     */
    private String getWeekKey(LocalDate date) {
        int year = date.getYear();
        int weekOfYear = getWeekOfYear(date);
        return String.format("%d-W%02d", year, weekOfYear);
    }

    /**
     * 주차 계산 (ISO 8601 기준)
     */
    private int getWeekOfYear(LocalDate date) {
        java.time.temporal.WeekFields weekFields = java.time.temporal.WeekFields.ISO;
        return date.get(weekFields.weekOfWeekBasedYear());
    }
}
