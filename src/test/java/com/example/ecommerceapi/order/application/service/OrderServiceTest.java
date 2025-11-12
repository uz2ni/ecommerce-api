package com.example.ecommerceapi.order.application.service;

import com.example.ecommerceapi.cart.domain.entity.CartItem;
import com.example.ecommerceapi.cart.domain.repository.CartItemRepository;
import com.example.ecommerceapi.common.exception.*;
import com.example.ecommerceapi.coupon.domain.entity.Coupon;
import com.example.ecommerceapi.coupon.domain.entity.CouponUser;
import com.example.ecommerceapi.coupon.domain.repository.CouponRepository;
import com.example.ecommerceapi.coupon.domain.repository.CouponUserRepository;
import com.example.ecommerceapi.order.application.dto.CreateOrderCommand;
import com.example.ecommerceapi.order.application.dto.CreateOrderResult;
import com.example.ecommerceapi.order.application.dto.OrderResult;
import com.example.ecommerceapi.order.application.dto.PaymentResult;
import com.example.ecommerceapi.order.domain.entity.Order;
import com.example.ecommerceapi.order.domain.entity.OrderItem;
import com.example.ecommerceapi.order.domain.entity.OrderStatus;
import com.example.ecommerceapi.order.domain.repository.OrderItemRepository;
import com.example.ecommerceapi.order.domain.repository.OrderRepository;
import com.example.ecommerceapi.point.domain.repository.PointRepository;
import com.example.ecommerceapi.product.application.validator.ProductValidator;
import com.example.ecommerceapi.product.domain.entity.Product;
import com.example.ecommerceapi.product.domain.repository.ProductRepository;
import com.example.ecommerceapi.user.application.validator.UserValidator;
import com.example.ecommerceapi.user.domain.entity.User;
import com.example.ecommerceapi.user.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService 단위 테스트")
class OrderServiceTest {

    @Mock
    private UserValidator userValidator;

    @Mock
    private ProductValidator productValidator;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private CouponUserRepository couponUserRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PointRepository pointRepository;

    @InjectMocks
    private OrderService orderService;

    private User user;
    private Product product1;
    private Product product2;
    private CartItem cartItem1;
    private CartItem cartItem2;
    private Coupon coupon;
    private CouponUser couponUser;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .userId(1)
                .username("테스트 사용자")
                .pointBalance(100000)
                .build();

        product1 = Product.builder()
                .productId(1)
                .productName("테스트 상품1")
                .description("상품 설명1")
                .productPrice(10000)
                .quantity(100)
                .build();

        product2 = Product.builder()
                .productId(2)
                .productName("테스트 상품2")
                .description("상품 설명2")
                .productPrice(20000)
                .quantity(50)
                .build();

        cartItem1 = CartItem.builder()
                .cartItemId(1)
                .user(user)
                .product(product1)
                .productName("테스트 상품1")
                .productPrice(10000)
                .quantity(2)
                .totalPrice(20000)
                .build();

        cartItem2 = CartItem.builder()
                .cartItemId(2)
                .user(user)
                .product(product2)
                .productName("테스트 상품2")
                .productPrice(20000)
                .quantity(1)
                .totalPrice(20000)
                .build();

        coupon = Coupon.builder()
                .couponId(1)
                .couponName("테스트 쿠폰")
                .discountAmount(5000)
                .totalQuantity(100)
                .issuedQuantity(50)
                .expiredAt(LocalDateTime.now().plusDays(7))
                .version(1)
                .build();

        couponUser = CouponUser.builder()
                .couponUserId(1)
                .coupon(Coupon.builder().couponId(1).version(1).build())
                .user(user)
                .used(false)
                .build();
    }

    @Nested
    @DisplayName("주문 생성 테스트")
    class CreateOrderTest {

        @Test
        @DisplayName("장바구니 상품으로 주문을 생성한다")
        void createOrder_ShouldCreateOrder_WithCartItems() {
            // given
            CreateOrderCommand command = new CreateOrderCommand(
                    1,
                    "홍길동",
                    "서울시 강남구",
                    null
            );

            List<CartItem> cartItems = Arrays.asList(cartItem1, cartItem2);

            given(userValidator.validateAndGetUser(1)).willReturn(user);
            given(cartItemRepository.findByUserId(1)).willReturn(cartItems);
            given(productValidator.validateAndGetProduct(1)).willReturn(product1);
            given(productValidator.validateAndGetProduct(2)).willReturn(product2);
            given(orderRepository.save(any(Order.class))).willAnswer(invocation -> {
                Order order = invocation.getArgument(0);
                order.setOrderId(1);
                return order;
            });
            given(productRepository.findById(1)).willReturn(product1);
            given(productRepository.findById(2)).willReturn(product2);
            given(orderItemRepository.save(any(OrderItem.class))).willAnswer(invocation -> {
                OrderItem item = invocation.getArgument(0);
                item.setOrderItemId(1);
                return item;
            });

            // when
            CreateOrderResult result = orderService.createOrder(command);

            // then
            assertThat(result).isNotNull();
            assertThat(result.userId()).isEqualTo(1);
            assertThat(result.orderStatus()).isEqualTo("PENDING");
            assertThat(result.createdAt()).isNotNull();
            verify(orderRepository).save(any(Order.class));
            verify(orderItemRepository, times(2)).save(any(OrderItem.class));
        }

        @Test
        @DisplayName("쿠폰을 적용하여 주문을 생성한다")
        void createOrder_ShouldCreateOrder_WithCoupon() {
            // given
            CreateOrderCommand command = new CreateOrderCommand(
                    1,
                    "홍길동",
                    "서울시 강남구",
                    1
            );

            List<CartItem> cartItems = Arrays.asList(cartItem1);

            given(userValidator.validateAndGetUser(1)).willReturn(user);
            given(cartItemRepository.findByUserId(1)).willReturn(cartItems);
            given(productValidator.validateAndGetProduct(1)).willReturn(product1);
            given(couponRepository.findById(1)).willReturn(Optional.of(coupon));
            given(couponUserRepository.findByCouponIdAndUserId(1, 1)).willReturn(Optional.of(couponUser));
            given(orderRepository.save(any(Order.class))).willAnswer(invocation -> {
                Order order = invocation.getArgument(0);
                order.setOrderId(1);
                return order;
            });
            given(productRepository.findById(1)).willReturn(product1);
            given(orderItemRepository.save(any(OrderItem.class))).willAnswer(invocation -> {
                OrderItem item = invocation.getArgument(0);
                item.setOrderItemId(1);
                return item;
            });

            // when
            CreateOrderResult result = orderService.createOrder(command);

            // then
            assertThat(result).isNotNull();
            assertThat(result.userId()).isEqualTo(1);
            assertThat(result.orderStatus()).isEqualTo("PENDING");
            assertThat(result.createdAt()).isNotNull();
            verify(orderRepository).save(any(Order.class));
        }

        @Test
        @DisplayName("장바구니가 비어있으면 예외가 발생한다")
        void createOrder_ShouldThrowException_WhenCartIsEmpty() {
            // given
            CreateOrderCommand command = new CreateOrderCommand(
                    1,
                    "홍길동",
                    "서울시 강남구",
                    null
            );

            given(userValidator.validateAndGetUser(1)).willReturn(user);
            given(cartItemRepository.findByUserId(1)).willReturn(Arrays.asList());

            // when & then
            assertThatThrownBy(() -> orderService.createOrder(command))
                    .isInstanceOf(CartException.class)
                    .hasMessage("장바구니가 비어있습니다.");
        }

        @Test
        @DisplayName("재고가 부족하면 예외가 발생한다")
        void createOrder_ShouldThrowException_WhenStockInsufficient() {
            // given
            CreateOrderCommand command = new CreateOrderCommand(
                    1,
                    "홍길동",
                    "서울시 강남구",
                    null
            );

            Product insufficientProduct = Product.builder()
                    .productId(1)
                    .productName("재고 부족 상품")
                    .quantity(1)
                    .build();

            CartItem insufficientCartItem = CartItem.builder()
                    .cartItemId(1)
                    .user(user)
                    .product(insufficientProduct)
                    .quantity(5)
                    .build();

            given(userValidator.validateAndGetUser(1)).willReturn(user);
            given(cartItemRepository.findByUserId(1)).willReturn(Arrays.asList(insufficientCartItem));
            given(productValidator.validateAndGetProduct(1)).willReturn(insufficientProduct);

            // when & then
            assertThatThrownBy(() -> orderService.createOrder(command))
                    .isInstanceOf(ProductException.class)
                    .hasMessage("상품 재고가 부족합니다.");
        }

        @Test
        @DisplayName("존재하지 않는 쿠폰으로 주문하면 예외가 발생한다")
        void createOrder_ShouldThrowException_WhenCouponNotFound() {
            // given
            CreateOrderCommand command = new CreateOrderCommand(
                    1,
                    "홍길동",
                    "서울시 강남구",
                    999
            );

            given(userValidator.validateAndGetUser(1)).willReturn(user);
            given(cartItemRepository.findByUserId(1)).willReturn(Arrays.asList(cartItem1));
            given(productValidator.validateAndGetProduct(1)).willReturn(product1);
            given(couponRepository.findById(999)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> orderService.createOrder(command))
                    .isInstanceOf(CouponException.class)
                    .hasMessage("존재하지 않는 쿠폰입니다.");
        }

        @Test
        @DisplayName("만료된 쿠폰으로 주문하면 예외가 발생한다")
        void createOrder_ShouldThrowException_WhenCouponExpired() {
            // given
            CreateOrderCommand command = new CreateOrderCommand(
                    1,
                    "홍길동",
                    "서울시 강남구",
                    1
            );

            Coupon expiredCoupon = Coupon.builder()
                    .couponId(1)
                    .couponName("만료된 쿠폰")
                    .discountAmount(5000)
                    .expiredAt(LocalDateTime.now().minusDays(1))
                    .version(1)
                    .build();

            given(userValidator.validateAndGetUser(1)).willReturn(user);
            given(cartItemRepository.findByUserId(1)).willReturn(Arrays.asList(cartItem1));
            given(productValidator.validateAndGetProduct(1)).willReturn(product1);
            given(couponRepository.findById(1)).willReturn(Optional.of(expiredCoupon));

            // when & then
            assertThatThrownBy(() -> orderService.createOrder(command))
                    .isInstanceOf(CouponException.class)
                    .hasMessage("만료된 쿠폰입니다.");
        }

        @Test
        @DisplayName("사용자가 발급받지 않은 쿠폰으로 주문하면 예외가 발생한다")
        void createOrder_ShouldThrowException_WhenCouponNotIssued() {
            // given
            CreateOrderCommand command = new CreateOrderCommand(
                    1,
                    "홍길동",
                    "서울시 강남구",
                    1
            );

            given(userValidator.validateAndGetUser(1)).willReturn(user);
            given(cartItemRepository.findByUserId(1)).willReturn(Arrays.asList(cartItem1));
            given(productValidator.validateAndGetProduct(1)).willReturn(product1);
            given(couponRepository.findById(1)).willReturn(Optional.of(coupon));
            given(couponUserRepository.findByCouponIdAndUserId(1, 1)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> orderService.createOrder(command))
                    .isInstanceOf(CouponException.class)
                    .hasMessage("발급받지 않은 쿠폰입니다.");
        }

        @Test
        @DisplayName("이미 사용한 쿠폰으로 주문하면 예외가 발생한다")
        void createOrder_ShouldThrowException_WhenCouponAlreadyUsed() {
            // given
            CreateOrderCommand command = new CreateOrderCommand(
                    1,
                    "홍길동",
                    "서울시 강남구",
                    1
            );

            CouponUser usedCouponUser = CouponUser.builder()
                    .couponUserId(1)
                    .coupon(Coupon.builder().couponId(1).version(1).build())
                    .user(user)
                    .used(true)
                    .build();

            given(userValidator.validateAndGetUser(1)).willReturn(user);
            given(cartItemRepository.findByUserId(1)).willReturn(Arrays.asList(cartItem1));
            given(productValidator.validateAndGetProduct(1)).willReturn(product1);
            given(couponRepository.findById(1)).willReturn(Optional.of(coupon));
            given(couponUserRepository.findByCouponIdAndUserId(1, 1)).willReturn(Optional.of(usedCouponUser));

            // when & then
            assertThatThrownBy(() -> orderService.createOrder(command))
                    .isInstanceOf(CouponException.class)
                    .hasMessage("이미 사용된 쿠폰입니다.");
        }

        @Test
        @DisplayName("존재하지 않는 사용자로 주문하면 예외가 발생한다")
        void createOrder_ShouldThrowException_WhenUserNotFound() {
            // given
            CreateOrderCommand command = new CreateOrderCommand(
                    999,
                    "홍길동",
                    "서울시 강남구",
                    null
            );

            given(userValidator.validateAndGetUser(999))
                    .willThrow(new UserException(ErrorCode.USER_NOT_FOUND));

            // when & then
            assertThatThrownBy(() -> orderService.createOrder(command))
                    .isInstanceOf(UserException.class)
                    .hasMessage("회원이 존재하지 않습니다.");
        }
    }

    @Nested
    @DisplayName("주문 조회 테스트")
    class GetOrderTest {

        @Test
        @DisplayName("주문 ID로 주문을 조회한다")
        void getOrder_ShouldReturnOrder_WhenOrderExists() {
            // given
            Order order = Order.builder()
                    .orderId(1)
                    .user(User.builder().userId(1).build())
                    .orderStatus(OrderStatus.PENDING)
                    .totalOrderAmount(40000)
                    .totalDiscountAmount(0)
                    .finalPaymentAmount(40000)
                    .deliveryUsername("홍길동")
                    .deliveryAddress("서울시 강남구")
                    .build();

            OrderItem orderItem1 = OrderItem.builder()
                    .orderItemId(1)
                    .order(Order.builder().orderId(1).build())
                    .product(product1)
                    .productName("테스트 상품1")
                    .productPrice(10000)
                    .orderQuantity(2)
                    .totalPrice(20000)
                    .build();

            OrderItem orderItem2 = OrderItem.builder()
                    .orderItemId(2)
                    .order(Order.builder().orderId(1).build())
                    .product(product2)
                    .productName("테스트 상품2")
                    .productPrice(20000)
                    .orderQuantity(1)
                    .totalPrice(20000)
                    .build();

            given(orderRepository.findById(1)).willReturn(Optional.of(order));
            given(orderItemRepository.findByOrderId(1)).willReturn(Arrays.asList(orderItem1, orderItem2));

            // when
            OrderResult result = orderService.getOrder(1);

            // then
            assertThat(result).isNotNull();
            assertThat(result.orderId()).isEqualTo(1);
            assertThat(result.userId()).isEqualTo(1);
            assertThat(result.totalOrderAmount()).isEqualTo(40000);
            assertThat(result.orderItems()).hasSize(2);
            verify(orderRepository).findById(1);
            verify(orderItemRepository).findByOrderId(1);
        }

        @Test
        @DisplayName("존재하지 않는 주문 조회 시 예외가 발생한다")
        void getOrder_ShouldThrowException_WhenOrderNotFound() {
            // given
            given(orderRepository.findById(999)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> orderService.getOrder(999))
                    .isInstanceOf(OrderException.class)
                    .hasMessage("존재하지 않는 주문입니다.");
        }
    }

    @Nested
    @DisplayName("결제 처리 테스트")
    class ProcessPaymentTest {

        @Test
        @DisplayName("주문을 결제 처리한다")
        void processPayment_ShouldProcessPayment_WithValidOrder() {
            // given
            Order order = Order.builder()
                    .orderId(1)
                    .user(User.builder().userId(1).build())
                    .orderStatus(OrderStatus.PENDING)
                    .finalPaymentAmount(40000)
                    .coupon(null)
                    .build();

            OrderItem orderItem = OrderItem.builder()
                    .orderItemId(1)
                    .order(Order.builder().orderId(1).build())
                    .product(product1)
                    .orderQuantity(2)
                    .build();

            given(orderRepository.findById(1)).willReturn(Optional.of(order));
            given(userValidator.validateAndGetUser(1)).willReturn(user);
            given(orderItemRepository.findByOrderId(1)).willReturn(Arrays.asList(orderItem));
            given(productRepository.findById(1)).willReturn(product1);
            given(cartItemRepository.findByUserId(1)).willReturn(Arrays.asList(cartItem1));
            given(orderRepository.save(any(Order.class))).willReturn(order);

            // when
            PaymentResult result = orderService.processPayment(1, 1);

            // then
            assertThat(result).isNotNull();
            assertThat(result.orderId()).isEqualTo(1);
            verify(userRepository).save(any(User.class));
            verify(pointRepository).save(any());
            verify(productRepository).save(any(Product.class));
            verify(cartItemRepository).deleteByUserId(1);
            verify(orderRepository).save(any(Order.class));
        }

        @Test
        @DisplayName("존재하지 않는 주문으로 결제하면 예외가 발생한다")
        void processPayment_ShouldThrowException_WhenOrderNotFound() {
            // given
            given(orderRepository.findById(999)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> orderService.processPayment(999, 1))
                    .isInstanceOf(OrderException.class)
                    .hasMessage("존재하지 않는 주문입니다.");
        }

        @Test
        @DisplayName("결제 불가능한 상태의 주문이면 예외가 발생한다")
        void processPayment_ShouldThrowException_WhenOrderStatusInvalid() {
            // given
            Order paidOrder = Order.builder()
                    .orderId(1)
                    .user(User.builder().userId(1).build())
                    .orderStatus(OrderStatus.PAID)
                    .finalPaymentAmount(40000)
                    .build();

            given(orderRepository.findById(1)).willReturn(Optional.of(paidOrder));

            // when & then
            assertThatThrownBy(() -> orderService.processPayment(1, 1))
                    .isInstanceOf(OrderException.class)
                    .hasMessage("결제 가능한 주문 상태가 아닙니다.");
        }

        @Test
        @DisplayName("포인트가 부족하면 예외가 발생한다")
        void processPayment_ShouldThrowException_WhenPointInsufficient() {
            // given
            User poorUser = User.builder()
                    .userId(1)
                    .username("가난한 사용자")
                    .pointBalance(1000)
                    .build();

            Order order = Order.builder()
                    .orderId(1)
                    .user(User.builder().userId(1).build())
                    .orderStatus(OrderStatus.PENDING)
                    .finalPaymentAmount(40000)
                    .coupon(null)
                    .build();

            given(orderRepository.findById(1)).willReturn(Optional.of(order));
            given(userValidator.validateAndGetUser(1)).willReturn(poorUser);

            // when & then
            assertThatThrownBy(() -> orderService.processPayment(1, 1))
                    .isInstanceOf(PointException.class)
                    .hasMessage("포인트 잔액이 부족합니다.");
        }
    }
}