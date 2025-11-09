package com.example.ecommerceapi.user.presentation.controller;

import com.example.ecommerceapi.user.domain.repository.UserRepository;
import com.example.ecommerceapi.point.domain.repository.PointRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("UserController 통합 테스트")
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;


    @BeforeEach
    void setUp() {
        // 각 테스트 전에 초기 상태로 리셋 (필요한 경우)
    }

    @Test
    @DisplayName("GET /api/users - 전체 회원 목록을 조회한다")
    void getAllUsers_ShouldReturnUserList() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].userId", notNullValue()))
                .andExpect(jsonPath("$[0].username", notNullValue()))
                .andExpect(jsonPath("$[0].pointBalance", notNullValue()));
    }

    @Test
    @DisplayName("GET /api/users/{userId} - 회원 정보를 조회한다")
    void getUser_ShouldReturnUser_WhenUserExists() throws Exception {
        // given: 초기 데이터에 userId=1이 존재한다고 가정

        // when & then
        mockMvc.perform(get("/api/users/{userId}", 1))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.userId", is(1)))
                .andExpect(jsonPath("$.username", notNullValue()))
                .andExpect(jsonPath("$.pointBalance", notNullValue()));
    }

    @Test
    @DisplayName("GET /api/users/{userId} - 존재하지 않는 회원 조회 시 예외가 발생한다")
    void getUser_ShouldReturnNotFound_WhenUserNotExists() throws Exception {
        mockMvc.perform(get("/api/users/{userId}", 9999))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code", is("US01")))
                .andExpect(jsonPath("$.message", is("회원이 존재하지 않습니다.")));
    }

    @Test
    @DisplayName("GET /api/users/{userId}/points/balance - 회원의 포인트 잔액을 조회한다")
    void getPointBalance_ShouldReturnBalance_WhenUserExists() throws Exception {
        mockMvc.perform(get("/api/users/{userId}/points/balance", 1))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.userId", is(1)))
                .andExpect(jsonPath("$.pointBalance", notNullValue()));
    }

    @Test
    @DisplayName("GET /api/users/{userId}/points/balance - 존재하지 않는 회원의 잔액 조회 시 예외가 발생한다")
    void getPointBalance_ShouldReturnNotFound_WhenUserNotExists() throws Exception {
        mockMvc.perform(get("/api/users/{userId}/points/balance", 9999))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code", is("US01")))
                .andExpect(jsonPath("$.message", is("회원이 존재하지 않습니다.")));
    }

    @Test
    @DisplayName("GET /api/users/{userId}/points/history - 회원의 포인트 이력을 조회한다")
    void getPointHistory_ShouldReturnHistory_WhenUserExists() throws Exception {
        mockMvc.perform(get("/api/users/{userId}/points/history", 1))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", isA(java.util.List.class)));
    }

    @Test
    @DisplayName("GET /api/users/{userId}/points/history - 존재하지 않는 회원의 이력 조회 시 예외가 발생한다")
    void getPointHistory_ShouldReturnNotFound_WhenUserNotExists() throws Exception {
        mockMvc.perform(get("/api/users/{userId}/points/history", 9999))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code", is("US01")))
                .andExpect(jsonPath("$.message", is("회원이 존재하지 않습니다.")));
    }

    @Test
    @DisplayName("POST /api/users/{userId}/points/charge - 포인트를 충전한다")
    void chargePoint_ShouldSucceed_WhenValidRequest() throws Exception {
        // given
        Map<String, Object> request = new HashMap<>();
        request.put("amount", 10000);

        // when & then
        mockMvc.perform(post("/api/users/{userId}/points/charge", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.userId", is(1)))
                .andExpect(jsonPath("$.pointType", is("CHARGE")))
                .andExpect(jsonPath("$.pointAmount", is(10000)))
                .andExpect(jsonPath("$.createdAt", notNullValue()));
    }

    @Test
    @DisplayName("POST /api/users/{userId}/points/charge - 최소 금액 미만 충전 시 예외가 발생한다")
    void chargePoint_ShouldFail_WhenAmountBelowMinimum() throws Exception {
        // given
        Map<String, Object> request = new HashMap<>();
        request.put("amount", 500);

        // when & then
        mockMvc.perform(post("/api/users/{userId}/points/charge", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code", is("FD01")))
                .andExpect(jsonPath("$.errorFields[0].field", is("amount")))
                .andExpect(jsonPath("$.errorFields[0].message", is("amount는 1000 이상이어야 합니다.")));
    }

    @Test
    @DisplayName("POST /api/users/{userId}/points/charge - 최대 금액 초과 충전 시 예외가 발생한다")
    void chargePoint_ShouldFail_WhenAmountAboveMaximum() throws Exception {
        // given
        Map<String, Object> request = new HashMap<>();
        request.put("amount", 1500000);

        // when & then
        mockMvc.perform(post("/api/users/{userId}/points/charge", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code", is("PT02")))
                .andExpect(jsonPath("$.message", is("포인트 금액이 유효하지 않습니다.")));
    }

    @Test
    @DisplayName("POST /api/users/{userId}/points/charge - 존재하지 않는 회원에게 충전 시 예외가 발생한다")
    void chargePoint_ShouldFail_WhenUserNotExists() throws Exception {
        // given
        Map<String, Object> request = new HashMap<>();
        request.put("amount", 10000);

        // when & then
        mockMvc.perform(post("/api/users/{userId}/points/charge", 9999)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code", is("US01")))
                .andExpect(jsonPath("$.message", is("회원이 존재하지 않습니다.")));
    }

    @Test
    @DisplayName("POST /api/users/{userId}/points/charge - 최소 금액으로 충전한다")
    void chargePoint_ShouldSucceed_WithMinimumAmount() throws Exception {
        // given
        Map<String, Object> request = new HashMap<>();
        request.put("amount", 1000);

        // when & then
        mockMvc.perform(post("/api/users/{userId}/points/charge", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.userId", is(1)))
                .andExpect(jsonPath("$.pointAmount", is(1000)));
    }

    @Test
    @DisplayName("POST /api/users/{userId}/points/charge - 최대 금액으로 충전한다")
    void chargePoint_ShouldSucceed_WithMaximumAmount() throws Exception {
        // given
        Map<String, Object> request = new HashMap<>();
        request.put("amount", 1000000);

        // when & then
        mockMvc.perform(post("/api/users/{userId}/points/charge", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.userId", is(1)))
                .andExpect(jsonPath("$.pointAmount", is(1000000)));
    }
}