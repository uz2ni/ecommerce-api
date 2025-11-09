package com.example.ecommerceapi.product.presentation.controller;

import com.example.ecommerceapi.product.infrastructure.InMemoryProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("ProductController 통합 테스트")
class ProductControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private InMemoryProductRepository productRepository;

    @BeforeEach
    void setUp() {
        // 각 테스트 전에 초기 상태로 리셋 (필요한 경우)
    }

    @Test
    @DisplayName("GET /api/products - 전체 상품 목록을 조회한다")
    void getAllProducts_ShouldReturnProductList() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].productId", notNullValue()))
                .andExpect(jsonPath("$[0].productName", notNullValue()))
                .andExpect(jsonPath("$[0].productPrice", notNullValue()));
    }

    @Test
    @DisplayName("GET /api/products/{productId} - 상품 정보를 조회한다")
    void getProduct_ShouldReturnProduct_WhenProductExists() throws Exception {
        // given: 초기 데이터에 productId=1이 존재한다고 가정

        // when & then
        mockMvc.perform(get("/api/products/{productId}", 1))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.productId", is(1)))
                .andExpect(jsonPath("$.productName", notNullValue()))
                .andExpect(jsonPath("$.description", notNullValue()))
                .andExpect(jsonPath("$.productPrice", notNullValue()));
    }

    @Test
    @DisplayName("GET /api/products/{productId} - 존재하지 않는 상품 조회 시 예외가 발생한다")
    void getProduct_ShouldReturnNotFound_WhenProductNotExists() throws Exception {
        mockMvc.perform(get("/api/products/{productId}", 9999))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code", is("PD01")))
                .andExpect(jsonPath("$.message", is("상품이 존재하지 않습니다.")));
    }

    @Test
    @DisplayName("GET /api/products/{productId}/stock - 상품 재고를 조회한다")
    void getProductStock_ShouldReturnStock_WhenProductExists() throws Exception {
        mockMvc.perform(get("/api/products/{productId}/stock", 1))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.productId", is(1)))
                .andExpect(jsonPath("$.quantity", notNullValue()));
    }

    @Test
    @DisplayName("GET /api/products/{productId}/stock - 존재하지 않는 상품의 재고 조회 시 예외가 발생한다")
    void getProductStock_ShouldReturnNotFound_WhenProductNotExists() throws Exception {
        mockMvc.perform(get("/api/products/{productId}/stock", 9999))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code", is("PD01")))
                .andExpect(jsonPath("$.message", is("상품이 존재하지 않습니다.")));
    }

    @Test
    @DisplayName("GET /api/products/popular - 조회수 기반 인기 상품 목록을 조회한다")
    void getPopularProducts_ShouldReturnPopularProducts_ByViews() throws Exception {
        mockMvc.perform(get("/api/products/popular")
                        .param("type", "VIEWS")
                        .param("limit", "5"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", isA(java.util.List.class)))
                .andExpect(jsonPath("$[0].productId", notNullValue()))
                .andExpect(jsonPath("$[0].productName", notNullValue()))
                .andExpect(jsonPath("$[0].viewCount", notNullValue()));
    }

    @Test
    @DisplayName("GET /api/products/popular - 기본 파라미터로 인기 상품을 조회한다")
    void getPopularProducts_ShouldReturnPopularProducts_WithDefaultParams() throws Exception {
        mockMvc.perform(get("/api/products/popular"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", isA(java.util.List.class)));
    }

    @Test
    @DisplayName("GET /api/products/popular - limit 파라미터에 따라 결과 개수를 제한한다")
    void getPopularProducts_ShouldLimitResults_WhenLimitParamProvided() throws Exception {
        mockMvc.perform(get("/api/products/popular")
                        .param("type", "VIEWS")
                        .param("limit", "3"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(lessThanOrEqualTo(3))));
    }

    @Test
    @DisplayName("PATCH /api/products/{productId}/view - 상품 조회수를 증가시킨다")
    void incrementProductViewCount_ShouldIncreaseViewCount_WhenProductExists() throws Exception {
        // given: 상품 1의 현재 조회수 확인
        Integer initialViewCount = productRepository.findById(1).getViewCount();

        // when & then
        mockMvc.perform(patch("/api/products/{productId}/view", 1))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.viewCount", is(initialViewCount + 1)));

        // 실제로 증가했는지 확인
        Integer updatedViewCount = productRepository.findById(1).getViewCount();
        assertThat(updatedViewCount).isEqualTo(initialViewCount + 1);
    }

    @Test
    @DisplayName("PATCH /api/products/{productId}/view - 존재하지 않는 상품의 조회수 증가 시 예외가 발생한다")
    void incrementProductViewCount_ShouldReturnNotFound_WhenProductNotExists() throws Exception {
        mockMvc.perform(patch("/api/products/{productId}/view", 9999))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code", is("PD01")))
                .andExpect(jsonPath("$.message", is("상품이 존재하지 않습니다.")));
    }

    @Test
    @DisplayName("PATCH /api/products/{productId}/view - 조회수를 여러 번 증가시킬 수 있다")
    void incrementProductViewCount_ShouldIncreaseMultipleTimes() throws Exception {
        // given
        Integer productId = 2;
        Integer initialViewCount = productRepository.findById(productId).getViewCount();

        // when: 3번 조회수 증가
        mockMvc.perform(patch("/api/products/{productId}/view", productId))
                .andExpect(status().isOk());
        mockMvc.perform(patch("/api/products/{productId}/view", productId))
                .andExpect(status().isOk());
        mockMvc.perform(patch("/api/products/{productId}/view", productId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.viewCount", is(initialViewCount + 3)));

        // then
        Integer finalViewCount = productRepository.findById(productId).getViewCount();
        assertThat(finalViewCount).isEqualTo(initialViewCount + 3);
    }

    private org.assertj.core.api.AbstractIntegerAssert<?> assertThat(Integer actual) {
        return org.assertj.core.api.Assertions.assertThat(actual);
    }
}