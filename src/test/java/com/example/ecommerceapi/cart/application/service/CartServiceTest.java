package com.example.ecommerceapi.cart.application.service;

import com.example.ecommerceapi.cart.application.dto.AddCartItemCommand;
import com.example.ecommerceapi.cart.application.dto.CartItemResult;
import com.example.ecommerceapi.cart.domain.entity.CartItem;
import com.example.ecommerceapi.cart.domain.repository.CartItemRepository;
import com.example.ecommerceapi.common.exception.CartException;
import com.example.ecommerceapi.common.exception.ProductException;
import com.example.ecommerceapi.common.exception.UserException;
import com.example.ecommerceapi.product.application.validator.ProductValidator;
import com.example.ecommerceapi.product.domain.entity.Product;
import com.example.ecommerceapi.user.application.validator.UserValidator;
import com.example.ecommerceapi.user.domain.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("CartService 단위 테스트")
class CartServiceTest {

    @Mock
    private UserValidator userValidator;

    @Mock
    private ProductValidator productValidator;

    @Mock
    private CartItemRepository cartItemRepository;

    @InjectMocks
    private CartService cartService;

    private User user;
    private Product product;
    private CartItem cartItem1;
    private CartItem cartItem2;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .userId(1)
                .username("테스트 사용자")
                .build();

        product = Product.builder()
                .productId(1)
                .productName("테스트 상품")
                .description("테스트 상품 설명")
                .productPrice(10000)
                .quantity(100)
                .viewCount(0)
                .version(1)
                .build();

        cartItem1 = CartItem.builder()
                .cartItemId(1)
                .userId(1)
                .productId(1)
                .productName("테스트 상품1")
                .productPrice(10000)
                .quantity(2)
                .totalPrice(20000)
                .build();

        cartItem2 = CartItem.builder()
                .cartItemId(2)
                .userId(1)
                .productId(2)
                .productName("테스트 상품2")
                .productPrice(20000)
                .quantity(1)
                .totalPrice(20000)
                .build();
    }

    @Nested
    @DisplayName("장바구니 목록 조회 테스트")
    class GetCartItemsTest {

        @Test
        @DisplayName("사용자의 장바구니 목록을 조회한다")
        void getCartItems_ShouldReturnCartItems_WhenUserExists() {
            // given
            List<CartItem> cartItems = Arrays.asList(cartItem1, cartItem2);
            given(userValidator.validateAndGetUser(1)).willReturn(user);
            given(cartItemRepository.findByUserId(1)).willReturn(cartItems);

            // when
            List<CartItemResult> result = cartService.getCartItems(1);

            // then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).cartItemId()).isEqualTo(1);
            assertThat(result.get(0).productName()).isEqualTo("테스트 상품1");
            assertThat(result.get(1).cartItemId()).isEqualTo(2);
            assertThat(result.get(1).productName()).isEqualTo("테스트 상품2");
            verify(userValidator).validateAndGetUser(1);
            verify(cartItemRepository).findByUserId(1);
        }

        @Test
        @DisplayName("장바구니가 비어있으면 빈 목록을 반환한다")
        void getCartItems_ShouldReturnEmptyList_WhenCartIsEmpty() {
            // given
            given(userValidator.validateAndGetUser(1)).willReturn(user);
            given(cartItemRepository.findByUserId(1)).willReturn(Arrays.asList());

            // when
            List<CartItemResult> result = cartService.getCartItems(1);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("존재하지 않는 사용자로 조회하면 예외가 발생한다")
        void getCartItems_ShouldThrowException_WhenUserNotExists() {
            // given
            given(userValidator.validateAndGetUser(999))
                    .willThrow(new UserException(com.example.ecommerceapi.common.exception.ErrorCode.USER_NOT_FOUND));

            // when & then
            assertThatThrownBy(() -> cartService.getCartItems(999))
                    .isInstanceOf(UserException.class)
                    .hasMessage("회원이 존재하지 않습니다.");
        }
    }

    @Nested
    @DisplayName("장바구니 상품 추가 테스트")
    class AddCartItemTest {

        @Test
        @DisplayName("새로운 상품을 장바구니에 추가한다")
        void addCartItem_ShouldAddNewItem_WhenItemNotExists() {
            // given
            AddCartItemCommand command = new AddCartItemCommand(
                    1,
                    1,
                    2
            );

            given(userValidator.validateAndGetUser(1)).willReturn(user);
            given(productValidator.validateAndGetProduct(1)).willReturn(product);
            given(cartItemRepository.findByUserIdAndProductId(1, 1)).willReturn(Optional.empty());
            given(cartItemRepository.save(any(CartItem.class))).willAnswer(invocation -> {
                CartItem item = invocation.getArgument(0);
                item.setCartItemId(1);
                return item;
            });

            // when
            CartItemResult result = cartService.addCartItem(command);

            // then
            assertThat(result).isNotNull();
            assertThat(result.userId()).isEqualTo(1);
            assertThat(result.productId()).isEqualTo(1);
            assertThat(result.productName()).isEqualTo("테스트 상품");
            assertThat(result.quantity()).isEqualTo(2);
            assertThat(result.totalPrice()).isEqualTo(20000);
            verify(cartItemRepository).save(any(CartItem.class));
        }

        @Test
        @DisplayName("이미 존재하는 상품은 수량을 덮어쓴다")
        void addCartItem_ShouldUpdateQuantity_WhenItemExists() {
            // given
            AddCartItemCommand command = new AddCartItemCommand(
                    1,
                    1,
                    5
            );

            given(userValidator.validateAndGetUser(1)).willReturn(user);
            given(productValidator.validateAndGetProduct(1)).willReturn(product);
            given(cartItemRepository.findByUserIdAndProductId(1, 1)).willReturn(Optional.of(cartItem1));
            given(cartItemRepository.save(cartItem1)).willReturn(cartItem1);

            // when
            CartItemResult result = cartService.addCartItem(command);

            // then
            assertThat(result).isNotNull();
            assertThat(result.quantity()).isEqualTo(5);
            assertThat(result.totalPrice()).isEqualTo(50000);
            verify(cartItemRepository).save(cartItem1);
        }

        @Test
        @DisplayName("재고를 초과하는 수량은 추가할 수 없다")
        void addCartItem_ShouldThrowException_WhenQuantityExceedsStock() {
            // given
            AddCartItemCommand command = new AddCartItemCommand(
                    1,
                    1,
                    150
            );

            given(userValidator.validateAndGetUser(1)).willReturn(user);
            given(productValidator.validateAndGetProduct(1)).willReturn(product);

            // when & then
            assertThatThrownBy(() -> cartService.addCartItem(command))
                    .isInstanceOf(ProductException.class)
                    .hasMessage("상품 재고가 부족합니다.");
        }

        @Test
        @DisplayName("존재하지 않는 사용자는 장바구니에 추가할 수 없다")
        void addCartItem_ShouldThrowException_WhenUserNotExists() {
            // given
            AddCartItemCommand command = new AddCartItemCommand(
                    999,
                    1,
                    2
            );

            given(userValidator.validateAndGetUser(999))
                    .willThrow(new UserException(com.example.ecommerceapi.common.exception.ErrorCode.USER_NOT_FOUND));

            // when & then
            assertThatThrownBy(() -> cartService.addCartItem(command))
                    .isInstanceOf(UserException.class)
                    .hasMessage("회원이 존재하지 않습니다.");
        }

        @Test
        @DisplayName("존재하지 않는 상품은 장바구니에 추가할 수 없다")
        void addCartItem_ShouldThrowException_WhenProductNotExists() {
            // given
            AddCartItemCommand command = new AddCartItemCommand(
                    1,
                    999,
                    2
            );

            given(userValidator.validateAndGetUser(1)).willReturn(user);
            given(productValidator.validateAndGetProduct(999))
                    .willThrow(new ProductException(com.example.ecommerceapi.common.exception.ErrorCode.PRODUCT_NOT_FOUND));

            // when & then
            assertThatThrownBy(() -> cartService.addCartItem(command))
                    .isInstanceOf(ProductException.class)
                    .hasMessage("존재하는 상품이 아닙니다.");
        }

        @Test
        @DisplayName("재고가 정확히 일치하는 수량은 추가할 수 있다")
        void addCartItem_ShouldAddItem_WhenQuantityEqualsStock() {
            // given
            AddCartItemCommand command = new AddCartItemCommand(
                    1,
                    1,
                    100
            );

            given(userValidator.validateAndGetUser(1)).willReturn(user);
            given(productValidator.validateAndGetProduct(1)).willReturn(product);
            given(cartItemRepository.findByUserIdAndProductId(1, 1)).willReturn(Optional.empty());
            given(cartItemRepository.save(any(CartItem.class))).willAnswer(invocation -> {
                CartItem item = invocation.getArgument(0);
                item.setCartItemId(1);
                return item;
            });

            // when
            CartItemResult result = cartService.addCartItem(command);

            // then
            assertThat(result).isNotNull();
            assertThat(result.quantity()).isEqualTo(100);
            assertThat(result.totalPrice()).isEqualTo(1000000);
        }
    }

    @Nested
    @DisplayName("장바구니 상품 삭제 테스트")
    class DeleteCartItemTest {

        @Test
        @DisplayName("장바구니 상품을 삭제한다")
        void deleteCartItem_ShouldDeleteItem_WhenItemExists() {
            // given
            given(cartItemRepository.findById(1)).willReturn(Optional.of(cartItem1));

            // when
            Integer result = cartService.deleteCartItem(1);

            // then
            assertThat(result).isEqualTo(1);
            verify(cartItemRepository).deleteById(1);
        }

        @Test
        @DisplayName("존재하지 않는 장바구니 상품 삭제 시 예외가 발생한다")
        void deleteCartItem_ShouldThrowException_WhenItemNotExists() {
            // given
            given(cartItemRepository.findById(999)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> cartService.deleteCartItem(999))
                    .isInstanceOf(CartException.class)
                    .hasMessage("존재하지 않는 장바구니 상품 ID 입니다.");
        }
    }
}