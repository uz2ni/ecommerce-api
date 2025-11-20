package com.example.ecommerceapi.cart.domain.entity;

import com.example.ecommerceapi.common.exception.CartException;
import com.example.ecommerceapi.product.domain.entity.Product;
import com.example.ecommerceapi.user.domain.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("CartItem 엔티티 단위 테스트")
class CartItemTest {

    private User user;
    private Product product;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .userId(1)
                .username("테스트 사용자")
                .pointBalance(100000)
                .version(0)
                .build();

        product = Product.builder()
                .productId(1)
                .productName("테스트 상품")
                .description("테스트 상품 설명")
                .productPrice(10000)
                .quantity(100)
                .viewCount(0)
                .build();
    }

    @Nested
    @DisplayName("장바구니 상품 생성 테스트")
    class CreateAddCartItemTest {

        @Test
        @DisplayName("유효한 값으로 장바구니 상품을 생성한다")
        void createAddCartItem_ShouldCreateCartItem_WithValidValues() {
            // given
            Integer quantity = 2;

            // when
            CartItem cartItem = CartItem.createAddCartItem(
                    user, product, quantity
            );

            // then
            assertThat(cartItem).isNotNull();
            assertThat(cartItem.getUserId()).isEqualTo(1);
            assertThat(cartItem.getProductId()).isEqualTo(1);
            assertThat(cartItem.getProductName()).isEqualTo("테스트 상품");
            assertThat(cartItem.getProductPrice()).isEqualTo(10000);
            assertThat(cartItem.getQuantity()).isEqualTo(quantity);
            assertThat(cartItem.getTotalPrice()).isEqualTo(20000);
            assertThat(cartItem.getCreatedAt()).isNotNull();
        }

        @Test
        @DisplayName("수량 1로 장바구니 상품을 생성한다")
        void createAddCartItem_ShouldCreateCartItem_WithQuantityOne() {
            // when
            CartItem cartItem = CartItem.createAddCartItem(
                    user, product, 1
            );

            // then
            assertThat(cartItem.getQuantity()).isEqualTo(1);
            assertThat(cartItem.getTotalPrice()).isEqualTo(10000);
        }

        @Test
        @DisplayName("수량이 0이면 예외가 발생한다")
        void createAddCartItem_ShouldThrowException_WhenQuantityIsZero() {
            // when & then
            assertThatThrownBy(() ->
                    CartItem.createAddCartItem(user, product, 0)
            )
                    .isInstanceOf(CartException.class)
                    .hasMessage("수량은 1 이상이어야 합니다.");
        }

        @Test
        @DisplayName("수량이 음수이면 예외가 발생한다")
        void createAddCartItem_ShouldThrowException_WhenQuantityIsNegative() {
            // when & then
            assertThatThrownBy(() ->
                    CartItem.createAddCartItem(user, product, -1)
            )
                    .isInstanceOf(CartException.class)
                    .hasMessage("수량은 1 이상이어야 합니다.");
        }

        @Test
        @DisplayName("수량이 null이면 예외가 발생한다")
        void createAddCartItem_ShouldThrowException_WhenQuantityIsNull() {
            // when & then
            assertThatThrownBy(() ->
                    CartItem.createAddCartItem(user, product, null)
            )
                    .isInstanceOf(CartException.class)
                    .hasMessage("수량은 1 이상이어야 합니다.");
        }

        @Test
        @DisplayName("총 금액이 정확히 계산된다")
        void createAddCartItem_ShouldCalculateTotalPriceCorrectly() {
            // given
            Product expensiveProduct = Product.builder()
                    .productId(2)
                    .productName("비싼 상품")
                    .description("비싼 상품 설명")
                    .productPrice(15000)
                    .quantity(100)
                    .viewCount(0)
                    .build();
            Integer quantity = 3;

            // when
            CartItem cartItem = CartItem.createAddCartItem(
                    user, expensiveProduct, quantity
            );

            // then
            assertThat(cartItem.getTotalPrice()).isEqualTo(45000);
        }
    }

    @Nested
    @DisplayName("수량 및 가격 변경 테스트")
    class ChangeQuantityAndPriceTest {

        @Test
        @DisplayName("수량을 변경하면 총 금액이 재계산된다")
        void changeQuantityAndPrice_ShouldRecalculateTotalPrice() {
            // given
            CartItem cartItem = CartItem.createAddCartItem(
                    user, product, 2
            );
            assertThat(cartItem.getTotalPrice()).isEqualTo(20000);

            // when
            cartItem.changeQuantityAndPrice(5);

            // then
            assertThat(cartItem.getQuantity()).isEqualTo(5);
            assertThat(cartItem.getTotalPrice()).isEqualTo(50000);
        }

        @Test
        @DisplayName("수량을 1로 변경할 수 있다")
        void changeQuantityAndPrice_ShouldChangeToOne() {
            // given
            CartItem cartItem = CartItem.createAddCartItem(
                    user, product, 5
            );

            // when
            cartItem.changeQuantityAndPrice(1);

            // then
            assertThat(cartItem.getQuantity()).isEqualTo(1);
            assertThat(cartItem.getTotalPrice()).isEqualTo(10000);
        }

        @Test
        @DisplayName("수량을 0으로 변경하면 예외가 발생한다")
        void changeQuantityAndPrice_ShouldThrowException_WhenQuantityIsZero() {
            // given
            CartItem cartItem = CartItem.createAddCartItem(
                    user, product, 2
            );

            // when & then
            assertThatThrownBy(() -> cartItem.changeQuantityAndPrice(0))
                    .isInstanceOf(CartException.class)
                    .hasMessage("수량은 1 이상이어야 합니다.");
        }

        @Test
        @DisplayName("수량을 음수로 변경하면 예외가 발생한다")
        void changeQuantityAndPrice_ShouldThrowException_WhenQuantityIsNegative() {
            // given
            CartItem cartItem = CartItem.createAddCartItem(
                    user, product, 2
            );

            // when & then
            assertThatThrownBy(() -> cartItem.changeQuantityAndPrice(-5))
                    .isInstanceOf(CartException.class)
                    .hasMessage("수량은 1 이상이어야 합니다.");
        }

        @Test
        @DisplayName("수량을 null로 변경하면 예외가 발생한다")
        void changeQuantityAndPrice_ShouldThrowException_WhenQuantityIsNull() {
            // given
            CartItem cartItem = CartItem.createAddCartItem(
                    user, product, 2
            );

            // when & then
            assertThatThrownBy(() -> cartItem.changeQuantityAndPrice(null))
                    .isInstanceOf(CartException.class)
                    .hasMessage("수량은 1 이상이어야 합니다.");
        }

        @Test
        @DisplayName("수량을 여러 번 변경할 수 있다")
        void changeQuantityAndPrice_ShouldChangeMultipleTimes() {
            // given
            CartItem cartItem = CartItem.createAddCartItem(
                    user, product, 1
            );

            // when
            cartItem.changeQuantityAndPrice(3);
            assertThat(cartItem.getQuantity()).isEqualTo(3);
            assertThat(cartItem.getTotalPrice()).isEqualTo(30000);

            cartItem.changeQuantityAndPrice(7);
            assertThat(cartItem.getQuantity()).isEqualTo(7);
            assertThat(cartItem.getTotalPrice()).isEqualTo(70000);

            cartItem.changeQuantityAndPrice(2);

            // then
            assertThat(cartItem.getQuantity()).isEqualTo(2);
            assertThat(cartItem.getTotalPrice()).isEqualTo(20000);
        }
    }
}