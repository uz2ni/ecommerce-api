package com.example.ecommerceapi.integration;

import com.example.ecommerceapi.domain.repository.PointRepository;
import com.example.ecommerceapi.domain.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "constant.point.min-amount=1000",
        "constant.point.max-amount=1000000"
})
@DisplayName("User-Point API 테스트")
class UserPointApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PointRepository pointRepository;

    private Integer testUserId;

    @BeforeEach
    void setUp() {
        testUserId = 1; // 기존 데이터 사용
    }

    @Nested
    @DisplayName("포인트 충전 API")
    class ChargePointApiTest {

        @Test
        @DisplayName("POST /api/users/{userId}/points/charge - 정상 충전")
        void chargePoint_Success() throws Exception {
            // given
            Integer chargeAmount = 5000;
            String requestBody = String.format("{\"amount\": %d}", chargeAmount);
            Integer initialBalance = userRepository.findBalanceById(testUserId);

            // when & then
            mockMvc.perform(post("/api/users/{userId}/points/charge", testUserId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.userId").value(testUserId))
                    .andExpect(jsonPath("$.pointType").value("CHARGE"))
                    .andExpect(jsonPath("$.pointAmount").value(chargeAmount))
                    .andExpect(jsonPath("$.createdAt").exists());

            // 잔액이 실제로 증가했는지 확인
            Integer updatedBalance = userRepository.findBalanceById(testUserId);
            assert updatedBalance.equals(initialBalance + chargeAmount);
        }

        @Test
        @DisplayName("POST /api/users/{userId}/points/charge - 최소 금액(1,000원) 경계값")
        void chargePoint_MinAmount_Success() throws Exception {
            // given
            Integer chargeAmount = 1000; // 최소 금액
            String requestBody = String.format("{\"amount\": %d}", chargeAmount);

            // when & then
            mockMvc.perform(post("/api/users/{userId}/points/charge", testUserId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.pointAmount").value(chargeAmount));
        }

        @Test
        @DisplayName("POST /api/users/{userId}/points/charge - 최대 금액(1,000,000원) 경계값")
        void chargePoint_MaxAmount_Success() throws Exception {
            // given
            Integer chargeAmount = 1000000; // 최대 금액
            String requestBody = String.format("{\"amount\": %d}", chargeAmount);

            // when & then
            mockMvc.perform(post("/api/users/{userId}/points/charge", testUserId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.pointAmount").value(chargeAmount));
        }

        @Test
        @DisplayName("POST /api/users/{userId}/points/charge - 최소 금액 미만(999원) 실패")
        void chargePoint_BelowMinAmount_BadRequest() throws Exception {
            // given
            Integer chargeAmount = 999; // 최소 금액 - 1
            String requestBody = String.format("{\"amount\": %d}", chargeAmount);
            Integer initialBalance = userRepository.findBalanceById(testUserId);

            // when & then
            mockMvc.perform(post("/api/users/{userId}/points/charge", testUserId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            // 잔액이 변경되지 않았는지 확인
            Integer updatedBalance = userRepository.findBalanceById(testUserId);
            assert updatedBalance.equals(initialBalance);
        }

        @Test
        @DisplayName("POST /api/users/{userId}/points/charge - 최대 금액 초과(1,000,001원) 실패")
        void chargePoint_AboveMaxAmount_BadRequest() throws Exception {
            // given
            Integer chargeAmount = 1000001; // 최대 금액 + 1
            String requestBody = String.format("{\"amount\": %d}", chargeAmount);
            Integer initialBalance = userRepository.findBalanceById(testUserId);

            // when & then
            mockMvc.perform(post("/api/users/{userId}/points/charge", testUserId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            // 잔액이 변경되지 않았는지 확인
            Integer updatedBalance = userRepository.findBalanceById(testUserId);
            assert updatedBalance.equals(initialBalance);
        }

        @Test
        @DisplayName("POST /api/users/{userId}/points/charge - null 금액 실패")
        void chargePoint_NullAmount_BadRequest() throws Exception {
            // given
            String requestBody = "{\"amount\": null}";
            Integer initialBalance = userRepository.findBalanceById(testUserId);

            // when & then
            mockMvc.perform(post("/api/users/{userId}/points/charge", testUserId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            // 잔액이 변경되지 않았는지 확인
            Integer updatedBalance = userRepository.findBalanceById(testUserId);
            assert updatedBalance.equals(initialBalance);
        }

        @Test
        @DisplayName("POST /api/users/{userId}/points/charge - 0원 충전 실패")
        void chargePoint_ZeroAmount_BadRequest() throws Exception {
            // given
            Integer chargeAmount = 0;
            String requestBody = String.format("{\"amount\": %d}", chargeAmount);
            Integer initialBalance = userRepository.findBalanceById(testUserId);

            // when & then
            mockMvc.perform(post("/api/users/{userId}/points/charge", testUserId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            // 잔액이 변경되지 않았는지 확인
            Integer updatedBalance = userRepository.findBalanceById(testUserId);
            assert updatedBalance.equals(initialBalance);
        }

        @Test
        @DisplayName("POST /api/users/{userId}/points/charge - 음수 금액 실패")
        void chargePoint_NegativeAmount_BadRequest() throws Exception {
            // given
            Integer chargeAmount = -5000;
            String requestBody = String.format("{\"amount\": %d}", chargeAmount);
            Integer initialBalance = userRepository.findBalanceById(testUserId);

            // when & then
            mockMvc.perform(post("/api/users/{userId}/points/charge", testUserId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            // 잔액이 변경되지 않았는지 확인
            Integer updatedBalance = userRepository.findBalanceById(testUserId);
            assert updatedBalance.equals(initialBalance);
        }

        @Test
        @DisplayName("POST /api/users/{userId}/points/charge - 존재하지 않는 사용자")
        void chargePoint_UserNotFound() throws Exception {
            // given
            Integer nonExistentUserId = 99999;
            Integer chargeAmount = 5000;
            String requestBody = String.format("{\"amount\": %d}", chargeAmount);

            // when & then
            mockMvc.perform(post("/api/users/{userId}/points/charge", nonExistentUserId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("POST /api/users/{userId}/points/charge - 잘못된 JSON 형식")
        void chargePoint_InvalidJson_BadRequest() throws Exception {
            // given
            String invalidJson = "{\"amount\": \"not-a-number\"}";

            // when & then
            mockMvc.perform(post("/api/users/{userId}/points/charge", testUserId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidJson))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("포인트 잔액 조회 API")
    class GetPointBalanceApiTest {

        @Test
        @DisplayName("GET /api/users/{userId}/points/balance - 정상 조회")
        void getPointBalance_Success() throws Exception {
            // when & then
            mockMvc.perform(get("/api/users/{userId}/points/balance", testUserId))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.userId").value(testUserId))
                    .andExpect(jsonPath("$.pointBalance").isNumber())
                    .andExpect(jsonPath("$.pointBalance").value(greaterThanOrEqualTo(0)));
        }

        @Test
        @DisplayName("GET /api/users/{userId}/points/balance - 충전 후 잔액 확인")
        void getPointBalance_AfterCharge() throws Exception {
            // given
            Integer chargeAmount = 7000;
            String requestBody = String.format("{\"amount\": %d}", chargeAmount);
            Integer initialBalance = userRepository.findBalanceById(testUserId);

            // 충전 실행
            mockMvc.perform(post("/api/users/{userId}/points/charge", testUserId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody));

            // when & then - 잔액 조회
            mockMvc.perform(get("/api/users/{userId}/points/balance", testUserId))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.userId").value(testUserId))
                    .andExpect(jsonPath("$.pointBalance").value(initialBalance + chargeAmount));
        }

        @Test
        @DisplayName("GET /api/users/{userId}/points/balance - 존재하지 않는 사용자")
        void getPointBalance_UserNotFound() throws Exception {
            // given
            Integer nonExistentUserId = 99999;

            // when & then
            mockMvc.perform(get("/api/users/{userId}/points/balance", nonExistentUserId))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("포인트 이력 조회 API")
    class GetPointHistoryApiTest {

        @Test
        @DisplayName("GET /api/users/{userId}/points/history - 정상 조회")
        void getPointHistory_Success() throws Exception {
            // when & then
            mockMvc.perform(get("/api/users/{userId}/points/history", testUserId))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }

        @Test
        @DisplayName("GET /api/users/{userId}/points/history - 충전 후 이력 확인")
        void getPointHistory_AfterCharge() throws Exception {
            // given
            Integer chargeAmount = 8000;
            String requestBody = String.format("{\"amount\": %d}", chargeAmount);
            int initialHistoryCount = pointRepository.findAllByUserId(testUserId).size();

            // 충전 실행
            mockMvc.perform(post("/api/users/{userId}/points/charge", testUserId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody));

            // when & then - 이력 조회
            mockMvc.perform(get("/api/users/{userId}/points/history", testUserId))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(initialHistoryCount + 1))
                    .andExpect(jsonPath("$[" + initialHistoryCount + "].userId").value(testUserId))
                    .andExpect(jsonPath("$[" + initialHistoryCount + "].pointType").value("CHARGE"))
                    .andExpect(jsonPath("$[" + initialHistoryCount + "].pointAmount").value(chargeAmount))
                    .andExpect(jsonPath("$[" + initialHistoryCount + "].createdAt").exists());
        }

        @Test
        @DisplayName("GET /api/users/{userId}/points/history - 존재하지 않는 사용자")
        void getPointHistory_UserNotFound() throws Exception {
            // given
            Integer nonExistentUserId = 99999;

            // when & then
            mockMvc.perform(get("/api/users/{userId}/points/history", nonExistentUserId))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("GET /api/users/{userId}/points/history - 여러 번 충전 후 모든 이력 확인")
        void getPointHistory_MultipleCharges() throws Exception {
            // given
            int initialHistoryCount = pointRepository.findAllByUserId(testUserId).size();
            Integer firstCharge = 3000;
            Integer secondCharge = 5000;
            Integer thirdCharge = 2000;

            // 여러 번 충전
            mockMvc.perform(post("/api/users/{userId}/points/charge", testUserId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(String.format("{\"amount\": %d}", firstCharge)));

            mockMvc.perform(post("/api/users/{userId}/points/charge", testUserId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(String.format("{\"amount\": %d}", secondCharge)));

            mockMvc.perform(post("/api/users/{userId}/points/charge", testUserId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(String.format("{\"amount\": %d}", thirdCharge)));

            // when & then - 이력 조회
            mockMvc.perform(get("/api/users/{userId}/points/history", testUserId))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(initialHistoryCount + 3));
        }
    }

    @Nested
    @DisplayName("회원 정보 조회 API")
    class GetUserApiTest {

        @Test
        @DisplayName("GET /api/users/{userId} - 정상 조회")
        void getUser_Success() throws Exception {
            // when & then
            mockMvc.perform(get("/api/users/{userId}", testUserId))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.userId").value(testUserId))
                    .andExpect(jsonPath("$.username").exists())
                    .andExpect(jsonPath("$.pointBalance").isNumber());
        }

        @Test
        @DisplayName("GET /api/users/{userId} - 존재하지 않는 사용자")
        void getUser_UserNotFound() throws Exception {
            // given
            Integer nonExistentUserId = 99999;

            // when & then
            mockMvc.perform(get("/api/users/{userId}", nonExistentUserId))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("전체 흐름 시나리오 테스트")
    class EndToEndScenarioTest {

        @Test
        @DisplayName("포인트 충전 → 잔액 조회 → 이력 조회 전체 흐름")
        void fullScenario_ChargeAndCheck() throws Exception {
            // given
            Integer chargeAmount = 10000;
            Integer initialBalance = userRepository.findBalanceById(testUserId);
            int initialHistoryCount = pointRepository.findAllByUserId(testUserId).size();

            // 1. 포인트 충전
            mockMvc.perform(post("/api/users/{userId}/points/charge", testUserId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(String.format("{\"amount\": %d}", chargeAmount)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.pointAmount").value(chargeAmount));

            // 2. 잔액 조회로 확인
            mockMvc.perform(get("/api/users/{userId}/points/balance", testUserId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.pointBalance").value(initialBalance + chargeAmount));

            // 3. 이력 조회로 확인
            mockMvc.perform(get("/api/users/{userId}/points/history", testUserId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(initialHistoryCount + 1))
                    .andExpect(jsonPath("$[" + initialHistoryCount + "].pointType").value("CHARGE"));

            // 4. 회원 정보 조회로 최종 확인
            mockMvc.perform(get("/api/users/{userId}", testUserId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.pointBalance").value(initialBalance + chargeAmount));
        }
    }
}