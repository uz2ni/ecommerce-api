package com.example.ecommerceapi.coupon.presentation.controller;

import com.example.ecommerceapi.coupon.domain.repository.CouponRepository;
import com.example.ecommerceapi.coupon.domain.repository.CouponUserRepository;
import com.example.ecommerceapi.coupon.presentation.dto.IssueCouponRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("CouponController 통합 테스트")
class CouponControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private CouponUserRepository couponUserRepository;

    @BeforeEach
    void setUp() {
        // 각 테스트 전에 초기 상태로 리셋
        couponRepository.clear();
        couponRepository.init();
        couponUserRepository.clear();
        couponUserRepository.init();
    }

    @Nested
    @DisplayName("쿠폰 목록 조회 테스트")
    class GetCouponsTest {

        @Test
        @DisplayName("GET /api/coupons - 쿠폰 목록을 조회한다")
        void getCoupons_ShouldReturnCouponList() throws Exception {
            mockMvc.perform(get("/api/coupons"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(3)))
                    .andExpect(jsonPath("$[0].couponId", notNullValue()))
                    .andExpect(jsonPath("$[0].couponName", notNullValue()))
                    .andExpect(jsonPath("$[0].discountAmount", notNullValue()))
                    .andExpect(jsonPath("$[0].totalQuantity", notNullValue()))
                    .andExpect(jsonPath("$[0].issuedQuantity", notNullValue()))
                    .andExpect(jsonPath("$[0].remainingQuantity", notNullValue()));
        }

        @Test
        @DisplayName("GET /api/coupons - 쿠폰 정보에 만료일이 포함된다")
        void getCoupons_ShouldIncludeExpiredAt() throws Exception {
            mockMvc.perform(get("/api/coupons"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].expiredAt", notNullValue()));
        }
    }

    @Nested
    @DisplayName("쿠폰 발급 테스트")
    class IssueCouponTest {

        @Test
        @DisplayName("POST /api/coupons/issue - 쿠폰을 발급한다")
        void issueCoupon_ShouldIssueCoupon_WhenValid() throws Exception {
            // given
            IssueCouponRequest request = new IssueCouponRequest(
                    5, // 초기 데이터에 없는 사용자
                    1
            );

            // when & then
            mockMvc.perform(post("/api/coupons/issue")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.couponUserId", notNullValue()))
                    .andExpect(jsonPath("$.couponId", is(1)))
                    .andExpect(jsonPath("$.userId", is(5)));
        }

        @Test
        @DisplayName("POST /api/coupons/issue - 이미 발급받은 쿠폰은 중복 발급할 수 없다")
        void issueCoupon_ShouldReturnBadRequest_WhenAlreadyIssued() throws Exception {
            // given: 초기 데이터에서 user1은 coupon1을 이미 발급받음
            IssueCouponRequest request = new IssueCouponRequest(
                    1,
                    1
            );

            // when & then
            mockMvc.perform(post("/api/coupons/issue")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.code", is("CP03")))
                    .andExpect(jsonPath("$.message", is("이미 발급받은 쿠폰입니다.")));
        }

        @Test
        @DisplayName("POST /api/coupons/issue - 품절된 쿠폰은 발급할 수 없다")
        void issueCoupon_ShouldReturnBadRequest_WhenSoldOut() throws Exception {
            // given: 초기 데이터에서 coupon2는 품절 상태 (totalQuantity=3, issuedQuantity=3)
            IssueCouponRequest request = new IssueCouponRequest(
                    5,
                    2
            );

            // when & then
            mockMvc.perform(post("/api/coupons/issue")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.code", is("CP02")))
                    .andExpect(jsonPath("$.message", is("발급 가능한 쿠폰이 아닙니다. 수량이 소진되었거나 만료되었습니다.")));
        }

        @Test
        @DisplayName("POST /api/coupons/issue - 존재하지 않는 사용자는 쿠폰을 발급받을 수 없다")
        void issueCoupon_ShouldReturnNotFound_WhenUserNotExists() throws Exception {
            // given
            IssueCouponRequest request = new IssueCouponRequest(
                    999,
                    1
            );

            // when & then
            mockMvc.perform(post("/api/coupons/issue")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.code", is("US01")))
                    .andExpect(jsonPath("$.message", is("회원이 존재하지 않습니다.")));
        }

        @Test
        @DisplayName("POST /api/coupons/issue - 존재하지 않는 쿠폰은 발급할 수 없다")
        void issueCoupon_ShouldReturnNotFound_WhenCouponNotExists() throws Exception {
            // given
            IssueCouponRequest request = new IssueCouponRequest(
                    1,
                    999
            );

            // when & then
            mockMvc.perform(post("/api/coupons/issue")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.code", is("CP01")))
                    .andExpect(jsonPath("$.message", is("존재하지 않는 쿠폰입니다.")));
        }

        @Test
        @DisplayName("POST /api/coupons/issue - couponId가 null이면 예외가 발생한다")
        void issueCoupon_ShouldReturnBadRequest_WhenCouponIdIsNull() throws Exception {
            // given
            IssueCouponRequest request = new IssueCouponRequest(
                    1,
                    null
            );

            // when & then
            mockMvc.perform(post("/api/coupons/issue")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.code", is("FD01")))
                    .andExpect(jsonPath("$.errorFields[0].field", is("couponId")))
                    .andExpect(jsonPath("$.errorFields[0].message", is("couponId는 필수입니다.")));
        }

        @Test
        @DisplayName("POST /api/coupons/issue - userId가 null이면 예외가 발생한다")
        void issueCoupon_ShouldReturnBadRequest_WhenUserIdIsNull() throws Exception {
            // given
            IssueCouponRequest request = new IssueCouponRequest(
                    null,
                    1
            );

            // when & then
            mockMvc.perform(post("/api/coupons/issue")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.code", is("FD01")))
                    .andExpect(jsonPath("$.errorFields[0].field", is("userId")))
                    .andExpect(jsonPath("$.errorFields[0].message", is("userId는 필수입니다.")));
        }
    }

    @Nested
    @DisplayName("쿠폰 사용 이력 조회 테스트")
    class GetCouponUsageHistoryTest {

        @Test
        @DisplayName("GET /api/coupons/{couponId}/usage - 쿠폰 사용 이력을 조회한다")
        void getCouponUsageHistory_ShouldReturnUsageHistory() throws Exception {
            // given: 초기 데이터에서 coupon1은 3명에게 발급됨
            mockMvc.perform(get("/api/coupons/{couponId}/usage", 1))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(3)))
                    .andExpect(jsonPath("$[0].couponUserId", notNullValue()))
                    .andExpect(jsonPath("$[0].userId", notNullValue()))
                    .andExpect(jsonPath("$[0].userName", notNullValue()))
                    .andExpect(jsonPath("$[0].used", notNullValue()))
                    .andExpect(jsonPath("$[0].issuedAt", notNullValue()));
        }

        @Test
        @DisplayName("GET /api/coupons/{couponId}/usage - 발급 이력이 없으면 빈 배열을 반환한다")
        void getCouponUsageHistory_ShouldReturnEmptyArray_WhenNoHistory() throws Exception {
            // given: 새로운 쿠폰을 추가하고 발급 이력이 없는 상태로 테스트
            // 초기 데이터의 coupon3은 발급 이력이 2개 있으므로, 다른 방법으로 테스트

            // 발급 이력을 모두 삭제하여 테스트
            couponUserRepository.clear();

            mockMvc.perform(get("/api/coupons/{couponId}/usage", 1))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(0)));
        }

        @Test
        @DisplayName("GET /api/coupons/{couponId}/usage - 존재하지 않는 쿠폰 ID로 조회 시 예외가 발생한다")
        void getCouponUsageHistory_ShouldReturnNotFound_WhenCouponNotExists() throws Exception {
            mockMvc.perform(get("/api/coupons/{couponId}/usage", 999))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.code", is("CP01")))
                    .andExpect(jsonPath("$.message", is("존재하지 않는 쿠폰입니다.")));
        }

        @Test
        @DisplayName("GET /api/coupons/{couponId}/usage - 사용된 쿠폰은 usedAt이 포함된다")
        void getCouponUsageHistory_ShouldIncludeUsedAt_WhenUsed() throws Exception {
            mockMvc.perform(get("/api/coupons/{couponId}/usage", 1))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[?(@.used == true)].usedAt").exists());
        }
    }

    @Nested
    @DisplayName("통합 시나리오 테스트")
    class IntegrationScenarioTest {

        @Test
        @DisplayName("쿠폰 조회 -> 발급 -> 이력 조회 시나리오")
        void fullCouponScenario() throws Exception {
            // 1. 쿠폰 목록 조회
            mockMvc.perform(get("/api/coupons"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(3)));

            // 2. 쿠폰 발급 (user 2는 coupon 3을 아직 발급받지 않음)
            IssueCouponRequest issueRequest = new IssueCouponRequest(
                    2,
                    3
            );

            String issueResponse = mockMvc.perform(post("/api/coupons/issue")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(issueRequest)))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();

            Integer couponUserId = objectMapper.readTree(issueResponse).get("couponUserId").asInt();
            assertTrue(couponUserId > 0);

            // 3. 쿠폰 사용 이력 조회 - 발급한 내역이 포함되어 있는지 확인
            mockMvc.perform(get("/api/coupons/{couponId}/usage", 3))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[?(@.couponUserId == " + couponUserId + ")].userId", contains(2)));

            // 4. 중복 발급 시도 - 실패해야 함
            mockMvc.perform(post("/api/coupons/issue")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(issueRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code", is("CP03")))
                    .andExpect(jsonPath("$.message", is("이미 발급받은 쿠폰입니다.")));
        }

        @Test
        @DisplayName("여러 사용자가 동일 쿠폰을 발급받을 수 있다")
        void multipleCouponIssuanceScenario() throws Exception {
            // given: coupon 3은 초기 데이터로 user 1, 4에게 발급됨
            Integer couponId = 3;

            // when: 여러 사용자가 동일 쿠폰 발급 (user 2, 3, 5는 아직 발급받지 않음)
            int[] userIds = {2, 3, 5};
            for (int userId : userIds) {
                IssueCouponRequest request = new IssueCouponRequest(
                        userId,
                        couponId
                );

                mockMvc.perform(post("/api/coupons/issue")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.couponId", is(couponId)))
                        .andExpect(jsonPath("$.userId", is(userId)));
            }

            // then: 쿠폰 사용 이력 조회 시 3명 + 초기 데이터 2명 = 총 5명
            mockMvc.perform(get("/api/coupons/{couponId}/usage", couponId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(5)));
        }
    }

    private org.hamcrest.Matcher<Iterable<? super Integer>> contains(int value) {
        return org.hamcrest.Matchers.hasItem(value);
    }

    private Integer assertThat(Integer actual) {
        org.assertj.core.api.Assertions.assertThat(actual).isNotNull();
        return actual;
    }
}