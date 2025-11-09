package com.example.ecommerceapi.order.application.service;

import com.example.ecommerceapi.cart.application.dto.AddCartItemCommand;
import com.example.ecommerceapi.cart.application.service.CartService;
import com.example.ecommerceapi.cart.domain.entity.CartItem;
import com.example.ecommerceapi.cart.domain.repository.CartItemRepository;
import com.example.ecommerceapi.common.exception.OrderException;
import com.example.ecommerceapi.common.exception.PointException;
import com.example.ecommerceapi.order.application.dto.CreateOrderCommand;
import com.example.ecommerceapi.order.application.dto.CreateOrderResult;
import com.example.ecommerceapi.order.domain.entity.Order;
import com.example.ecommerceapi.order.domain.entity.OrderStatus;
import com.example.ecommerceapi.order.domain.repository.OrderRepository;
import com.example.ecommerceapi.product.domain.entity.Product;
import com.example.ecommerceapi.product.domain.repository.ProductRepository;
import com.example.ecommerceapi.user.domain.entity.User;
import com.example.ecommerceapi.user.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DisplayName("OrderService 동시성 통합 테스트")
class OrderServiceConcurrencyIntegrationTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private CartService cartService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    private static final int THREAD_COUNT = 10;

    @BeforeEach
    void setUp() {
        // 각 테스트 전에 초기 상태로 리셋
        orderRepository.clear();
        cartItemRepository.clear();
        cartItemRepository.init();
        userRepository.clear();
        userRepository.init();
        productRepository.clear();
        productRepository.init();
    }

    @Test
    @DisplayName("동시에 같은 주문에 대해 결제 시도 시 첫 번째만 성공한다")
    void processPayment_ShouldProcessOnlyFirst_WhenConcurrentPaymentForSameOrder() throws InterruptedException {
        // given: 주문 생성
        Integer userId = 1;
        CreateOrderCommand command = CreateOrderCommand.builder()
                .userId(userId)
                .deliveryUsername("홍길동")
                .deliveryAddress("서울시 강남구")
                .couponId(null)
                .build();

        CreateOrderResult orderResult = orderService.createOrder(command);
        Integer orderId = orderResult.getOrderId();

        User initialUser = userRepository.findById(userId);
        Integer initialPointBalance = initialUser.getPointBalance();

        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // when: 동시에 같은 주문에 대해 결제 시도
        for (int i = 0; i < THREAD_COUNT; i++) {
            executorService.submit(() -> {
                try {
                    orderService.processPayment(orderId, userId);
                    successCount.incrementAndGet();
                } catch (OrderException e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // then: 정확히 1번만 성공
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failCount.get()).isEqualTo(THREAD_COUNT - 1);

        // 주문 상태 확인
        Order order = orderRepository.findById(orderId).orElseThrow();
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.PAID);

        // 포인트가 정확히 1번만 차감되었는지 확인
        User finalUser = userRepository.findById(userId);
        Integer expectedBalance = initialPointBalance - order.getFinalPaymentAmount();
        assertThat(finalUser.getPointBalance()).isEqualTo(expectedBalance);

        System.out.println("성공 횟수: " + successCount.get());
        System.out.println("실패 횟수: " + failCount.get());
        System.out.println("초기 포인트: " + initialPointBalance);
        System.out.println("최종 포인트: " + finalUser.getPointBalance());
    }

    @Test
    @DisplayName("여러 사용자가 동시에 서로 다른 주문을 결제하면 모두 성공한다")
    void processPayment_ShouldAllSucceed_WhenDifferentUsersPayDifferentOrders() throws InterruptedException {
        // given: 여러 사용자가 각각 주문 생성
        int userCount = 3;
        Integer[] orderIds = new Integer[userCount];
        Integer[] userIds = new Integer[userCount];

        for (int i = 0; i < userCount; i++) {
            Integer userId = i + 1;
            userIds[i] = userId;

            // 장바구니에 상품 추가
            AddCartItemCommand addCartCommand = AddCartItemCommand.builder()
                    .userId(userId)
                    .productId(1)
                    .quantity(1)
                    .build();
            cartService.addCartItem(addCartCommand);

            // 주문 생성
            CreateOrderCommand command = CreateOrderCommand.builder()
                    .userId(userId)
                    .deliveryUsername("사용자" + userId)
                    .deliveryAddress("주소" + userId)
                    .couponId(null)
                    .build();

            CreateOrderResult orderResult = orderService.createOrder(command);
            orderIds[i] = orderResult.getOrderId();
        }

        CountDownLatch latch = new CountDownLatch(userCount);
        ExecutorService executorService = Executors.newFixedThreadPool(userCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // when: 동시에 각자 다른 주문 결제
        for (int i = 0; i < userCount; i++) {
            final int index = i;
            executorService.submit(() -> {
                try {
                    orderService.processPayment(orderIds[index], userIds[index]);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                    System.out.println("결제 실패: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // then: 모두 성공
        assertThat(successCount.get()).isEqualTo(userCount);
        assertThat(failCount.get()).isEqualTo(0);

        // 모든 주문이 PAID 상태인지 확인
        for (Integer orderId : orderIds) {
            Order order = orderRepository.findById(orderId).orElseThrow();
            assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.PAID);
        }

        System.out.println("성공 횟수: " + successCount.get());
        System.out.println("실패 횟수: " + failCount.get());
    }

    @Test
    @DisplayName("포인트 부족으로 결제 실패 시 보상 트랜잭션이 실행되어 상태가 롤백된다")
    void processPayment_ShouldRollback_WhenPaymentFailsDueToInsufficientPoints() throws InterruptedException {
        // given: 포인트가 부족한 사용자로 주문 생성
        Integer userId = 1;
        User user = userRepository.findById(userId);

        // 사용자 포인트를 매우 작은 금액으로 설정
        user.setPointBalance(100);
        userRepository.save(user);

        // 주문 생성
        CreateOrderCommand command = CreateOrderCommand.builder()
                .userId(userId)
                .deliveryUsername("홍길동")
                .deliveryAddress("서울시 강남구")
                .couponId(null)
                .build();

        CreateOrderResult orderResult = orderService.createOrder(command);
        Integer orderId = orderResult.getOrderId();

        // 초기 상태 저장
        Integer initialPointBalance = user.getPointBalance();
        Product product = productRepository.findById(1);
        Integer initialProductStock = product.getQuantity();
        List<CartItem> initialCartItems = cartItemRepository.findByUserId(userId);
        Integer initialCartItemCount = initialCartItems.size();

        // when: 결제 시도 (실패할 것임)
        try {
            orderService.processPayment(orderId, userId);
        } catch (PointException e) {
            // 예상된 예외 (포인트 부족)
        }

        // then: 보상 트랜잭션으로 상태가 롤백되어야 함

        // 1. 포인트가 원래대로 돌아와야 함
        User finalUser = userRepository.findById(userId);
        assertThat(finalUser.getPointBalance()).isEqualTo(initialPointBalance);

        // 2. 상품 재고가 원래대로 돌아와야 함
        Product finalProduct = productRepository.findById(1);
        assertThat(finalProduct.getQuantity()).isEqualTo(initialProductStock);

        // 3. 주문 상태가 PENDING 또는 FAILED여야 함
        Order order = orderRepository.findById(orderId).orElseThrow();
        assertThat(order.getOrderStatus()).isIn(OrderStatus.PENDING, OrderStatus.FAILED);

        System.out.println("초기 포인트: " + initialPointBalance);
        System.out.println("최종 포인트: " + finalUser.getPointBalance());
        System.out.println("초기 재고: " + initialProductStock);
        System.out.println("최종 재고: " + finalProduct.getQuantity());
        System.out.println("주문 상태: " + order.getOrderStatus());
    }

    @Test
    @DisplayName("결제 성공 후 장바구니가 비워지고 재고가 차감된다")
    void processPayment_ShouldClearCartAndReduceStock_WhenPaymentSucceeds() {
        // given: 주문 생성
        Integer userId = 1;
        CreateOrderCommand command = CreateOrderCommand.builder()
                .userId(userId)
                .deliveryUsername("홍길동")
                .deliveryAddress("서울시 강남구")
                .couponId(null)
                .build();

        CreateOrderResult orderResult = orderService.createOrder(command);
        Integer orderId = orderResult.getOrderId();

        // 초기 상태 저장
        List<CartItem> initialCartItems = cartItemRepository.findByUserId(userId);
        assertThat(initialCartItems).isNotEmpty();

        Product product = productRepository.findById(1);
        Integer initialProductStock = product.getQuantity();

        User user = userRepository.findById(userId);
        Integer initialPointBalance = user.getPointBalance();

        // when: 결제 처리
        orderService.processPayment(orderId, userId);

        // then
        // 1. 장바구니가 비워져야 함
        List<CartItem> finalCartItems = cartItemRepository.findByUserId(userId);
        assertThat(finalCartItems).isEmpty();

        // 2. 재고가 차감되어야 함
        Product finalProduct = productRepository.findById(1);
        assertThat(finalProduct.getQuantity()).isLessThan(initialProductStock);

        // 3. 포인트가 차감되어야 함
        User finalUser = userRepository.findById(userId);
        assertThat(finalUser.getPointBalance()).isLessThan(initialPointBalance);

        // 4. 주문 상태가 PAID여야 함
        Order order = orderRepository.findById(orderId).orElseThrow();
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.PAID);

        System.out.println("초기 장바구니 항목 수: " + initialCartItems.size());
        System.out.println("최종 장바구니 항목 수: " + finalCartItems.size());
        System.out.println("초기 재고: " + initialProductStock);
        System.out.println("최종 재고: " + finalProduct.getQuantity());
    }
}