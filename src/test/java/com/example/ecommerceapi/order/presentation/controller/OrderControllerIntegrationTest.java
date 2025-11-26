package com.example.ecommerceapi.order.presentation.controller;

import com.example.ecommerceapi.cart.application.service.CartService;
import com.example.ecommerceapi.cart.domain.repository.CartItemRepository;
import com.example.ecommerceapi.common.AbstractIntegrationTest;
import com.example.ecommerceapi.coupon.domain.repository.CouponRepository;
import com.example.ecommerceapi.coupon.domain.repository.CouponUserRepository;
import com.example.ecommerceapi.order.domain.repository.OrderItemRepository;
import com.example.ecommerceapi.order.domain.repository.OrderRepository;
import com.example.ecommerceapi.order.presentation.dto.CreateOrderRequest;
import com.example.ecommerceapi.order.presentation.dto.PaymentRequest;
import com.example.ecommerceapi.point.application.service.PointService;
import com.example.ecommerceapi.product.domain.repository.ProductRepository;
import com.example.ecommerceapi.user.application.service.UserService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("OrderController 통합 테스트")
class OrderControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private CartService cartService;

    @Autowired
    private UserService userService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private CouponUserRepository couponUserRepository;

    @Autowired
    private PointService pointService;

    @BeforeEach
    void setUp() {
        // 각 테스트 전에 초기 상태로 리셋
        orderRepository.clear();
        orderItemRepository.clear();
        cartItemRepository.clear();
        userService.init();
        productRepository.clear();
        productRepository.init();
        couponRepository.clear();
        couponRepository.init();
        couponUserRepository.clear();
        couponUserRepository.init();
        pointService.init();
        cartService.init();

        clearAllRedisKeys();
    }

    @Nested
    @DisplayName("주문 생성 테스트")
    class CreateOrderTest {

        @Test
        @DisplayName("POST /api/orders - 장바구니 상품으로 주문을 생성한다")
        void createOrder_ShouldCreateOrder_WithCartItems() throws Exception {
            // given
            CreateOrderRequest request = new CreateOrderRequest(
                    1,
                    "홍길동",
                    "서울시 강남구",
                    null
            );

            // when & then
            mockMvc.perform(post("/api/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.orderId", notNullValue()))
                    .andExpect(jsonPath("$.userId", is(1)))
                    .andExpect(jsonPath("$.orderStatus", is("PENDING")));
        }

        @Test
        @DisplayName("POST /api/orders - 쿠폰을 적용하여 주문을 생성한다")
        void createOrder_ShouldCreateOrder_WithCoupon() throws Exception {
            // given
            CreateOrderRequest request = new CreateOrderRequest(
                    1,
                    "홍길동",
                    "서울시 강남구",
                    2
            );

            // when & then
            mockMvc.perform(post("/api/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.orderId", notNullValue()))
                    .andExpect(jsonPath("$.orderStatus", is("PENDING")));
        }

        @Test
        @DisplayName("POST /api/orders - 장바구니가 비어있으면 예외가 발생한다")
        void createOrder_ShouldReturnBadRequest_WhenCartIsEmpty() throws Exception {
            // given
            CreateOrderRequest request = new CreateOrderRequest(
                    3,
                    "홍길동",
                    "서울시 강남구",
                    null
            );

            // when & then
            mockMvc.perform(post("/api/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.code", is("CT04")))
                    .andExpect(jsonPath("$.message", is("장바구니가 비어있습니다.")));
        }

        @Test
        @DisplayName("POST /api/orders - userId가 null이면 예외가 발생한다")
        void createOrder_ShouldReturnBadRequest_WhenUserIdIsNull() throws Exception {
            // given
            CreateOrderRequest request = new CreateOrderRequest(
                    null,
                    "홍길동",
                    "서울시 강남구",
                    null
            );

            // when & then
            mockMvc.perform(post("/api/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.code", is("FD01")))
                    .andExpect(jsonPath("$.errorFields[0].field", is("userId")))
                    .andExpect(jsonPath("$.errorFields[0].message", is("userId는 필수입니다.")));
        }

        @Test
        @DisplayName("POST /api/orders - deliveryUsername이 빈 값이면 예외가 발생한다")
        void createOrder_ShouldReturnBadRequest_WhenDeliveryUsernameIsBlank() throws Exception {
            // given
            CreateOrderRequest request = new CreateOrderRequest(
                    1,
                    "",
                    "서울시 강남구",
                    null
            );

            // when & then
            mockMvc.perform(post("/api/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.code", is("FD01")))
                    .andExpect(jsonPath("$.errorFields[0].field", is("deliveryUsername")))
                    .andExpect(jsonPath("$.errorFields[0].message", is("deliveryUsername는 빈 값일 수 없습니다.")));
        }

        @Test
        @DisplayName("POST /api/orders - deliveryAddress가 빈 값이면 예외가 발생한다")
        void createOrder_ShouldReturnBadRequest_WhenDeliveryAddressIsBlank() throws Exception {
            // given
            CreateOrderRequest request = new CreateOrderRequest(
                    1,
                    "홍길동",
                    "",
                    null
            );

            // when & then
            mockMvc.perform(post("/api/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.code", is("FD01")))
                    .andExpect(jsonPath("$.errorFields[0].field", is("deliveryAddress")))
                    .andExpect(jsonPath("$.errorFields[0].message", is("deliveryAddress는 빈 값일 수 없습니다.")));
        }

        @Test
        @DisplayName("POST /api/orders - 존재하지 않는 사용자로 주문하면 예외가 발생한다")
        void createOrder_ShouldReturnNotFound_WhenUserNotExists() throws Exception {
            // given
            CreateOrderRequest request = new CreateOrderRequest(
                    999,
                    "홍길동",
                    "서울시 강남구",
                    null
            );

            // when & then
            mockMvc.perform(post("/api/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.code", is("US01")))
                    .andExpect(jsonPath("$.message", is("회원이 존재하지 않습니다.")));
        }

        @Test
        @DisplayName("POST /api/orders - 존재하지 않는 쿠폰으로 주문하면 예외가 발생한다")
        void createOrder_ShouldReturnNotFound_WhenCouponNotExists() throws Exception {
            // given
            CreateOrderRequest request = new CreateOrderRequest(
                    1,
                    "홍길동",
                    "서울시 강남구",
                    999
            );

            // when & then
            mockMvc.perform(post("/api/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.code", is("CP01")))
                    .andExpect(jsonPath("$.message", is("존재하지 않는 쿠폰입니다.")));
        }
    }

    @Nested
    @DisplayName("주문 조회 테스트")
    class GetOrderTest {

        @Test
        @DisplayName("GET /api/orders/{orderId} - 주문을 조회한다")
        void getOrder_ShouldReturnOrder_WhenOrderExists() throws Exception {
            // given: 먼저 주문 생성
            CreateOrderRequest createRequest = new CreateOrderRequest(
                    1,
                    "홍길동",
                    "서울시 강남구",
                    null
            );

            String createResponse = mockMvc.perform(post("/api/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();

            Integer orderId = objectMapper.readTree(createResponse).get("orderId").asInt();

            // when & then
            mockMvc.perform(get("/api/orders/{orderId}", orderId))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.orderId", is(orderId)))
                    .andExpect(jsonPath("$.userId", is(1)))
                    .andExpect(jsonPath("$.orderStatus", is("PENDING")))
                    .andExpect(jsonPath("$.orderItems", hasSize(greaterThan(0))));
        }

        @Test
        @DisplayName("GET /api/orders/{orderId} - 존재하지 않는 주문 조회 시 예외가 발생한다")
        void getOrder_ShouldReturnNotFound_WhenOrderNotExists() throws Exception {
            mockMvc.perform(get("/api/orders/{orderId}", 999))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.code", is("OD01")))
                    .andExpect(jsonPath("$.message", is("존재하지 않는 주문입니다.")));
        }
    }

    @Nested
    @DisplayName("결제 처리 테스트")
    class PaymentTest {

        @Test
        @DisplayName("POST /api/orders/payment - 주문을 결제 처리한다")
        void payment_ShouldProcessPayment_WhenOrderIsPending() throws Exception {
            // given: 먼저 주문 생성
            CreateOrderRequest createRequest = new CreateOrderRequest(
                    1,
                    "홍길동",
                    "서울시 강남구",
                    null
            );

            String createResponse = mockMvc.perform(post("/api/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();

            Integer orderId = objectMapper.readTree(createResponse).get("orderId").asInt();

            PaymentRequest paymentRequest = new PaymentRequest(
                    orderId,
                    1
            );

            // when & then
            mockMvc.perform(post("/api/orders/payment")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(paymentRequest)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.orderId", is(orderId)))
                    .andExpect(jsonPath("$.orderStatus", is("PAID")))
                    .andExpect(jsonPath("$.paymentAmount", notNullValue()))
                    .andExpect(jsonPath("$.remainingPoint", notNullValue()));
        }

        @Test
        @DisplayName("POST /api/orders/payment - orderId가 null이면 예외가 발생한다")
        void payment_ShouldReturnBadRequest_WhenOrderIdIsNull() throws Exception {
            // given
            PaymentRequest paymentRequest = new PaymentRequest(
                    null,
                    1
            );

            // when & then
            mockMvc.perform(post("/api/orders/payment")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(paymentRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.code", is("FD01")))
                    .andExpect(jsonPath("$.errorFields[0].field", is("orderId")))
                    .andExpect(jsonPath("$.errorFields[0].message", is("orderId는 필수입니다.")));
        }

        @Test
        @DisplayName("POST /api/orders/payment - userId가 null이면 예외가 발생한다")
        void payment_ShouldReturnBadRequest_WhenUserIdIsNull() throws Exception {
            // given
            PaymentRequest paymentRequest = new PaymentRequest(
                    1,
                    null
            );

            // when & then
            mockMvc.perform(post("/api/orders/payment")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(paymentRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.code", is("FD01")))
                    .andExpect(jsonPath("$.errorFields[0].field", is("userId")))
                    .andExpect(jsonPath("$.errorFields[0].message", is("userId는 필수입니다.")));
        }

        @Test
        @DisplayName("POST /api/orders/payment - 존재하지 않는 주문으로 결제하면 예외가 발생한다")
        void payment_ShouldReturnNotFound_WhenOrderNotExists() throws Exception {
            // given
            PaymentRequest paymentRequest = new PaymentRequest(
                    999,
                    1
            );

            // when & then
            mockMvc.perform(post("/api/orders/payment")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(paymentRequest)))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.code", is("OD01")))
                    .andExpect(jsonPath("$.message", is("존재하지 않는 주문입니다.")));
        }

        @Test
        @DisplayName("POST /api/orders/payment - 이미 결제된 주문은 예외가 발생한다")
        void payment_ShouldReturnBadRequest_WhenOrderAlreadyPaid() throws Exception {
            // given: 주문 생성 후 결제
            CreateOrderRequest createRequest = new CreateOrderRequest(
                    1,
                    "홍길동",
                    "서울시 강남구",
                    null
            );

            String createResponse = mockMvc.perform(post("/api/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();

            Integer orderId = objectMapper.readTree(createResponse).get("orderId").asInt();

            PaymentRequest paymentRequest = new PaymentRequest(
                    orderId,
                    1
            );

            // 첫 번째 결제 성공
            mockMvc.perform(post("/api/orders/payment")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(paymentRequest)))
                    .andExpect(status().isOk());

            // when & then: 두 번째 결제 시도 시 실패
            mockMvc.perform(post("/api/orders/payment")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(paymentRequest)))
                    .andDo(print())
                    .andExpect(status().isConflict())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.code", is("OD03")))
                    .andExpect(jsonPath("$.message", is("결제 가능한 주문 상태가 아닙니다.")));
        }
    }

    @Nested
    @DisplayName("통합 시나리오 테스트")
    class IntegrationScenarioTest {

        @Test
        @DisplayName("주문 생성 -> 조회 -> 결제 시나리오")
        void fullOrderScenario() throws Exception {
            // 1. 주문 생성
            CreateOrderRequest createRequest = new CreateOrderRequest(
                    1,
                    "홍길동",
                    "서울시 강남구",
                    null
            );

            String createResponse = mockMvc.perform(post("/api/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.orderStatus", is("PENDING")))
                    .andReturn().getResponse().getContentAsString();

            Integer orderId = objectMapper.readTree(createResponse).get("orderId").asInt();

            // 2. 주문 조회
            mockMvc.perform(get("/api/orders/{orderId}", orderId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.orderId", is(orderId)))
                    .andExpect(jsonPath("$.userId", is(1)))
                    .andExpect(jsonPath("$.orderStatus", is("PENDING")));

            // 3. 결제 처리
            PaymentRequest paymentRequest = new PaymentRequest(
                    orderId,
                    1
            );

            mockMvc.perform(post("/api/orders/payment")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(paymentRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.orderId", is(orderId)))
                    .andExpect(jsonPath("$.orderStatus", is("PAID")))
                    .andExpect(jsonPath("$.paymentAmount", is(50700)))
                    .andExpect(jsonPath("$.remainingPoint", is(449300)));

            // 4. 결제 후 주문 조회 - PAID 상태 확인
            mockMvc.perform(get("/api/orders/{orderId}", orderId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.orderId", is(orderId)))
                    .andExpect(jsonPath("$.orderStatus", is("PAID")));
        }

        @Test
        @DisplayName("쿠폰 적용 주문 -> 결제 시나리오")
        void orderWithCouponScenario() throws Exception {
            // 1. 쿠폰 적용 주문 생성
            CreateOrderRequest createRequest = new CreateOrderRequest(
                    2,
                    "홍길동",
                    "서울시 강남구",
                    1
            );

            String createResponse = mockMvc.perform(post("/api/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.userId", is(2)))
                    .andExpect(jsonPath("$.orderStatus", is("PENDING")))
                    .andReturn().getResponse().getContentAsString();

            Integer orderId = objectMapper.readTree(createResponse).get("orderId").asInt();

            // 2. 결제 처리
            PaymentRequest paymentRequest = new PaymentRequest(
                    orderId,
                    1
            );

            mockMvc.perform(post("/api/orders/payment")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(paymentRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.orderStatus", is("PAID")));
        }
    }
}