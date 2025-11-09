package com.example.ecommerceapi.cart.presentation.controller;

import com.example.ecommerceapi.cart.domain.repository.CartItemRepository;
import com.example.ecommerceapi.cart.presentation.dto.AddCartItemRequest;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("CartController 통합 테스트")
class CartControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CartItemRepository cartItemRepository;

    @BeforeEach
    void setUp() {
        // 각 테스트 전에 초기 상태로 리셋
        cartItemRepository.clear();
        cartItemRepository.init();
    }

    @Nested
    @DisplayName("장바구니 목록 조회 테스트")
    class GetCartItemsTest {

        @Test
        @DisplayName("GET /api/cart - 사용자의 장바구니 목록을 조회한다")
        void getCartItems_ShouldReturnCartItems_WhenUserHasItems() throws Exception {
            mockMvc.perform(get("/api/cart")
                            .param("userId", "1"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].cartItemId", notNullValue()))
                    .andExpect(jsonPath("$[0].userId", is(1)))
                    .andExpect(jsonPath("$[0].productName", notNullValue()))
                    .andExpect(jsonPath("$[0].quantity", notNullValue()))
                    .andExpect(jsonPath("$[0].totalPrice", notNullValue()));
        }

        @Test
        @DisplayName("GET /api/cart - 장바구니가 비어있으면 빈 배열을 반환한다")
        void getCartItems_ShouldReturnEmptyArray_WhenCartIsEmpty() throws Exception {
            mockMvc.perform(get("/api/cart")
                            .param("userId", "3"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(0)));
        }

        @Test
        @DisplayName("GET /api/cart - 존재하지 않는 사용자로 조회하면 예외가 발생한다")
        void getCartItems_ShouldReturnNotFound_WhenUserNotExists() throws Exception {
            mockMvc.perform(get("/api/cart")
                            .param("userId", "999"))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.code", is("US01")))
                    .andExpect(jsonPath("$.message", is("회원이 존재하지 않습니다.")));
        }
    }

    @Nested
    @DisplayName("장바구니 상품 추가 테스트")
    class AddCartItemTest {

        @Test
        @DisplayName("POST /api/cart - 새로운 상품을 장바구니에 추가한다")
        void addCartItem_ShouldAddNewItem_WhenItemNotExists() throws Exception {
            // given
            AddCartItemRequest request = AddCartItemRequest.builder()
                    .userId(1)
                    .productId(5)
                    .quantity(3)
                    .build();

            // when & then
            mockMvc.perform(post("/api/cart")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.userId", is(1)))
                    .andExpect(jsonPath("$.productId", is(5)))
                    .andExpect(jsonPath("$.quantity", is(3)))
                    .andExpect(jsonPath("$.cartItemId", notNullValue()))
                    .andExpect(jsonPath("$.totalPrice", notNullValue()));
        }

        @Test
        @DisplayName("POST /api/cart - 이미 존재하는 상품은 수량을 덮어쓴다")
        void addCartItem_ShouldUpdateQuantity_WhenItemExists() throws Exception {
            // given
            AddCartItemRequest request = AddCartItemRequest.builder()
                    .userId(1)
                    .productId(1)
                    .quantity(5)
                    .build();

            // when & then
            mockMvc.perform(post("/api/cart")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.userId", is(1)))
                    .andExpect(jsonPath("$.productId", is(1)))
                    .andExpect(jsonPath("$.quantity", is(5)));
        }

        @Test
        @DisplayName("POST /api/cart - 재고를 초과하는 수량은 추가할 수 없다")
        void addCartItem_ShouldReturnBadRequest_WhenQuantityExceedsStock() throws Exception {
            // given
            AddCartItemRequest request = AddCartItemRequest.builder()
                    .userId(1)
                    .productId(1)
                    .quantity(10000)
                    .build();

            // when & then
            mockMvc.perform(post("/api/cart")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isConflict())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.code", is("PD03")))
                    .andExpect(jsonPath("$.message", is("상품 재고가 부족합니다.")));
        }

        @Test
        @DisplayName("POST /api/cart - 수량이 0이면 예외가 발생한다")
        void addCartItem_ShouldReturnBadRequest_WhenQuantityIsZero() throws Exception {
            // given
            AddCartItemRequest request = AddCartItemRequest.builder()
                    .userId(1)
                    .productId(1)
                    .quantity(0)
                    .build();

            // when & then
            mockMvc.perform(post("/api/cart")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.code", is("FD01")))
                    .andExpect(jsonPath("$.errorFields[0].field", is("quantity")))
                    .andExpect(jsonPath("$.errorFields[0].message", is("quantity는 1 이상이어야 합니다.")));
        }

        @Test
        @DisplayName("POST /api/cart - userId가 null이면 예외가 발생한다")
        void addCartItem_ShouldReturnBadRequest_WhenUserIdIsNull() throws Exception {
            // given
            AddCartItemRequest request = AddCartItemRequest.builder()
                    .userId(null)
                    .productId(1)
                    .quantity(2)
                    .build();

            // when & then
            mockMvc.perform(post("/api/cart")
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
        @DisplayName("POST /api/cart - productId가 null이면 예외가 발생한다")
        void addCartItem_ShouldReturnBadRequest_WhenProductIdIsNull() throws Exception {
            // given
            AddCartItemRequest request = AddCartItemRequest.builder()
                    .userId(1)
                    .productId(null)
                    .quantity(2)
                    .build();

            // when & then
            mockMvc.perform(post("/api/cart")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.code", is("FD01")))
                    .andExpect(jsonPath("$.errorFields[0].field", is("productId")))
                    .andExpect(jsonPath("$.errorFields[0].message", is("productId는 필수입니다.")));
        }

        @Test
        @DisplayName("POST /api/cart - 존재하지 않는 사용자는 장바구니에 추가할 수 없다")
        void addCartItem_ShouldReturnNotFound_WhenUserNotExists() throws Exception {
            // given
            AddCartItemRequest request = AddCartItemRequest.builder()
                    .userId(999)
                    .productId(1)
                    .quantity(2)
                    .build();

            // when & then
            mockMvc.perform(post("/api/cart")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.code", is("US01")))
                    .andExpect(jsonPath("$.message", is("회원이 존재하지 않습니다.")));
        }

        @Test
        @DisplayName("POST /api/cart - 존재하지 않는 상품은 장바구니에 추가할 수 없다")
        void addCartItem_ShouldReturnNotFound_WhenProductNotExists() throws Exception {
            // given
            AddCartItemRequest request = AddCartItemRequest.builder()
                    .userId(1)
                    .productId(999)
                    .quantity(2)
                    .build();

            // when & then
            mockMvc.perform(post("/api/cart")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.code", is("PD01")))
                    .andExpect(jsonPath("$.message", is("존재하는 상품이 아닙니다.")));
        }
    }

    @Nested
    @DisplayName("장바구니 상품 삭제 테스트")
    class DeleteCartItemTest {

        @Test
        @DisplayName("DELETE /api/cart/{cartItemId} - 장바구니 상품을 삭제한다")
        void deleteCartItem_ShouldDeleteItem_WhenItemExists() throws Exception {
            // given: 초기 데이터에 cartItemId=1이 존재한다고 가정

            // when & then
            mockMvc.perform(delete("/api/cart/{cartItemId}", 1))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.cartItemId", is(1)));

            // 삭제 확인
            assertThat(cartItemRepository.findById(1)).isEmpty();
        }

        @Test
        @DisplayName("DELETE /api/cart/{cartItemId} - 존재하지 않는 장바구니 상품 삭제 시 예외가 발생한다")
        void deleteCartItem_ShouldReturnNotFound_WhenItemNotExists() throws Exception {
            mockMvc.perform(delete("/api/cart/{cartItemId}", 9999))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.code", is("CT03")))
                    .andExpect(jsonPath("$.message", is("존재하지 않는 장바구니 상품 ID 입니다.")));
        }

        @Test
        @DisplayName("DELETE /api/cart/{cartItemId} - 삭제 후 장바구니에서 조회되지 않는다")
        void deleteCartItem_ShouldNotBeFoundAfterDeletion() throws Exception {
            // given
            Integer cartItemId = 1;

            // when: 삭제
            mockMvc.perform(delete("/api/cart/{cartItemId}", cartItemId))
                    .andExpect(status().isOk());

            // then: 다시 삭제 시도하면 404
            mockMvc.perform(delete("/api/cart/{cartItemId}", cartItemId))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code", is("CT03")));
        }
    }

    @Nested
    @DisplayName("통합 시나리오 테스트")
    class IntegrationScenarioTest {

        @Test
        @DisplayName("장바구니 추가 -> 조회 -> 삭제 시나리오")
        void fullCartScenario() throws Exception {
            // 1. 장바구니에 상품 추가
            AddCartItemRequest addRequest = AddCartItemRequest.builder()
                    .userId(3)
                    .productId(1)
                    .quantity(2)
                    .build();

            String addResponse = mockMvc.perform(post("/api/cart")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(addRequest)))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();

            Integer cartItemId = objectMapper.readTree(addResponse).get("cartItemId").asInt();

            // 2. 장바구니 조회 - 추가한 상품이 있는지 확인
            mockMvc.perform(get("/api/cart")
                            .param("userId", "3"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].cartItemId", is(cartItemId)))
                    .andExpect(jsonPath("$[0].quantity", is(2)));

            // 3. 수량 변경 (덮어쓰기)
            addRequest.setQuantity(5);
            mockMvc.perform(post("/api/cart")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(addRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.quantity", is(5)));

            // 4. 장바구니에서 삭제
            mockMvc.perform(delete("/api/cart/{cartItemId}", cartItemId))
                    .andExpect(status().isOk());

            // 5. 장바구니 조회 - 비어있는지 확인
            mockMvc.perform(get("/api/cart")
                            .param("userId", "3"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }
    }

    private org.assertj.core.api.OptionalAssert<?> assertThat(java.util.Optional<?> actual) {
        return org.assertj.core.api.Assertions.assertThat(actual);
    }
}