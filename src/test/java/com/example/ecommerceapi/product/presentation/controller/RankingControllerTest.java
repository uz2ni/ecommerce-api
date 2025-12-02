package com.example.ecommerceapi.product.presentation.controller;

import com.example.ecommerceapi.product.application.dto.SalesRankingResult;
import com.example.ecommerceapi.product.application.service.RankingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RankingController.class)
@DisplayName("RankingController 단위 테스트")
class RankingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RankingService rankingService;

    @Test
    @DisplayName("일간 판매 랭킹 조회 - 날짜 지정")
    void getDailySalesRankings_WithDate() throws Exception {
        // given
        LocalDate date = LocalDate.of(2025, 12, 3);
        List<SalesRankingResult> rankings = Arrays.asList(
                new SalesRankingResult(1, "상품1", 100L, 1L),
                new SalesRankingResult(2, "상품2", 80L, 2L),
                new SalesRankingResult(3, "상품3", 60L, 3L)
        );

        given(rankingService.getDailySalesRankings(date, 10)).willReturn(rankings);

        // when & then
        mockMvc.perform(get("/api/rankings/sales/daily")
                        .param("date", "2025-12-03")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].productId").value(1))
                .andExpect(jsonPath("$[0].productName").value("상품1"))
                .andExpect(jsonPath("$[0].totalSalesCount").value(100))
                .andExpect(jsonPath("$[0].rank").value(1))
                .andExpect(jsonPath("$[1].productId").value(2))
                .andExpect(jsonPath("$[2].productId").value(3));
    }

    @Test
    @DisplayName("일간 판매 랭킹 조회 - 날짜 미지정 (오늘 날짜)")
    void getDailySalesRankings_WithoutDate() throws Exception {
        // given
        List<SalesRankingResult> rankings = Arrays.asList(
                new SalesRankingResult(5, "상품5", 50L, 1L)
        );

        given(rankingService.getDailySalesRankings(any(LocalDate.class), eq(10))).willReturn(rankings);

        // when & then
        mockMvc.perform(get("/api/rankings/sales/daily")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].productId").value(5));
    }

    @Test
    @DisplayName("일간 판매 랭킹 조회 - limit만 지정")
    void getDailySalesRankings_WithLimit() throws Exception {
        // given
        List<SalesRankingResult> rankings = Arrays.asList(
                new SalesRankingResult(1, "상품1", 100L, 1L),
                new SalesRankingResult(2, "상품2", 80L, 2L),
                new SalesRankingResult(3, "상품3", 60L, 3L),
                new SalesRankingResult(4, "상품4", 40L, 4L),
                new SalesRankingResult(5, "상품5", 20L, 5L)
        );

        given(rankingService.getDailySalesRankings(any(LocalDate.class), eq(5))).willReturn(rankings);

        // when & then
        mockMvc.perform(get("/api/rankings/sales/daily")
                        .param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(5)));
    }

    @Test
    @DisplayName("일간 판매 랭킹 조회 - 빈 결과")
    void getDailySalesRankings_EmptyResult() throws Exception {
        // given
        LocalDate date = LocalDate.of(2025, 12, 3);
        given(rankingService.getDailySalesRankings(date, 10)).willReturn(Collections.emptyList());

        // when & then
        mockMvc.perform(get("/api/rankings/sales/daily")
                        .param("date", "2025-12-03")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("주간 판매 랭킹 조회 - 날짜 지정")
    void getWeeklySalesRankings_WithDate() throws Exception {
        // given
        LocalDate date = LocalDate.of(2025, 12, 3);
        List<SalesRankingResult> rankings = Arrays.asList(
                new SalesRankingResult(10, "상품10", 500L, 1L),
                new SalesRankingResult(11, "상품11", 450L, 2L)
        );

        given(rankingService.getWeeklySalesRankings(date, 10)).willReturn(rankings);

        // when & then
        mockMvc.perform(get("/api/rankings/sales/weekly")
                        .param("date", "2025-12-03")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].productId").value(10))
                .andExpect(jsonPath("$[0].productName").value("상품10"))
                .andExpect(jsonPath("$[0].totalSalesCount").value(500))
                .andExpect(jsonPath("$[0].rank").value(1));
    }

    @Test
    @DisplayName("주간 판매 랭킹 조회 - 날짜 미지정 (현재 주)")
    void getWeeklySalesRankings_WithoutDate() throws Exception {
        // given
        List<SalesRankingResult> rankings = Arrays.asList(
                new SalesRankingResult(7, "상품7", 300L, 1L)
        );

        given(rankingService.getWeeklySalesRankings(any(LocalDate.class), eq(10))).willReturn(rankings);

        // when & then
        mockMvc.perform(get("/api/rankings/sales/weekly")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].productId").value(7));
    }

    @Test
    @DisplayName("주간 판매 랭킹 조회 - 빈 결과")
    void getWeeklySalesRankings_EmptyResult() throws Exception {
        // given
        LocalDate date = LocalDate.of(2025, 12, 3);
        given(rankingService.getWeeklySalesRankings(date, 10)).willReturn(Collections.emptyList());

        // when & then
        mockMvc.perform(get("/api/rankings/sales/weekly")
                        .param("date", "2025-12-03")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("일간 판매 랭킹 조회 - 기본 limit 값 적용")
    void getDailySalesRankings_DefaultLimit() throws Exception {
        // given
        List<SalesRankingResult> rankings = Arrays.asList(
                new SalesRankingResult(1, "상품1", 100L, 1L)
        );

        given(rankingService.getDailySalesRankings(any(LocalDate.class), eq(10))).willReturn(rankings);

        // when & then
        mockMvc.perform(get("/api/rankings/sales/daily"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @DisplayName("주간 판매 랭킹 조회 - 기본 limit 값 적용")
    void getWeeklySalesRankings_DefaultLimit() throws Exception {
        // given
        List<SalesRankingResult> rankings = Arrays.asList(
                new SalesRankingResult(1, "상품1", 100L, 1L)
        );

        given(rankingService.getWeeklySalesRankings(any(LocalDate.class), eq(10))).willReturn(rankings);

        // when & then
        mockMvc.perform(get("/api/rankings/sales/weekly"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }
}
