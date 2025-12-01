package com.example.ecommerceapi.order.application.service;

import com.example.ecommerceapi.cart.application.dto.AddCartItemCommand;
import com.example.ecommerceapi.cart.application.service.CartService;
import com.example.ecommerceapi.common.AbstractIntegrationTest;
import com.example.ecommerceapi.order.application.dto.CreateOrderCommand;
import com.example.ecommerceapi.order.application.dto.CreateOrderResult;
import com.example.ecommerceapi.order.application.dto.OrderResult;
import com.example.ecommerceapi.product.domain.entity.Product;
import com.example.ecommerceapi.product.domain.repository.ProductRepository;
import com.example.ecommerceapi.user.domain.entity.User;
import com.example.ecommerceapi.user.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("OrderService 캐싱 통합 테스트")
class OrderCacheIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private CartService cartService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        // Redis의 모든 키(캐시 + 락) 초기화
        clearAllRedisKeys();
    }

    @Test
    @Transactional
    @DisplayName("주문 조회 시 캐싱이 적용되어야 한다")
    void getOrder_ShouldBeCached() {
        // given - 주문 생성
        User user = userRepository.findAll().stream().findFirst().orElseThrow();
        Product product = productRepository.findAll().stream().findFirst().orElseThrow();

        // 장바구니에 상품 추가
        AddCartItemCommand cartCommand = new AddCartItemCommand(user.getUserId(), product.getProductId(), 1);
        cartService.addCartItem(cartCommand);

        // 주문 생성
        CreateOrderCommand orderCommand = new CreateOrderCommand(
                user.getUserId(),
                "홍길동",
                "서울시 강남구",
                null
        );
        CreateOrderResult createResult = orderService.createOrder(orderCommand);
        Integer orderId = createResult.orderId();
        String cacheName = "order";

        // when - 첫 번째 호출
        OrderResult firstCall = orderService.getOrder(orderId);

        // when - 두 번째 호출
        OrderResult secondCall = orderService.getOrder(orderId);

        // then - 동일한 데이터가 반환되어야 함 (캐시에서 조회)
        assertThat(firstCall.orderId()).isEqualTo(secondCall.orderId());
        assertThat(firstCall.userId()).isEqualTo(secondCall.userId());
        assertThat(firstCall.deliveryUsername()).isEqualTo(secondCall.deliveryUsername());
        assertThat(firstCall.orderStatus()).isEqualTo(secondCall.orderStatus());
    }

    @Test
    @Transactional
    @DisplayName("캐시를 삭제하면 다시 DB에서 조회해야 한다")
    void clearCache_ShouldReloadFromDatabase() {
        // given - 주문 생성
        User user = userRepository.findAll().stream().findFirst().orElseThrow();
        Product product = productRepository.findAll().stream().findFirst().orElseThrow();

        // 장바구니에 상품 추가
        AddCartItemCommand cartCommand = new AddCartItemCommand(user.getUserId(), product.getProductId(), 1);
        cartService.addCartItem(cartCommand);

        // 주문 생성
        CreateOrderCommand orderCommand = new CreateOrderCommand(
                user.getUserId(),
                "홍길동",
                "서울시 강남구",
                null
        );
        CreateOrderResult createResult = orderService.createOrder(orderCommand);
        Integer orderId = createResult.orderId();
        String cacheName = "order";

        // when - 첫 번째 호출로 캐시에 저장
        OrderResult firstCall = orderService.getOrder(orderId);

        // when - 캐시 삭제
        cacheManager.getCache(cacheName).clear();

        // when - 다시 조회
        OrderResult secondCall = orderService.getOrder(orderId);

        // then - 데이터가 올바르게 조회됨
        assertThat(secondCall.orderId()).isEqualTo(orderId);
        assertThat(secondCall.userId()).isEqualTo(firstCall.userId());
    }

    @Test
    @Transactional
    @DisplayName("서로 다른 주문 ID로 조회 시 각각 캐싱되어야 한다")
    void getOrder_WithDifferentIds_ShouldCacheSeparately() {
        // given - 첫 번째 사용자의 주문 생성
        List<User> users = userRepository.findAll();
        assertThat(users.size()).isGreaterThanOrEqualTo(2);
        User user1 = users.get(0);
        User user2 = users.get(1);
        Product product = productRepository.findAll().stream().findFirst().orElseThrow();

        // 첫 번째 주문
        AddCartItemCommand cartCommand1 = new AddCartItemCommand(user1.getUserId(), product.getProductId(), 1);
        cartService.addCartItem(cartCommand1);
        CreateOrderCommand orderCommand1 = new CreateOrderCommand(user1.getUserId(), "홍길동", "서울시 강남구", null);
        CreateOrderResult createResult1 = orderService.createOrder(orderCommand1);
        Integer orderId1 = createResult1.orderId();

        // 두 번째 주문 (다른 사용자)
        AddCartItemCommand cartCommand2 = new AddCartItemCommand(user2.getUserId(), product.getProductId(), 2);
        cartService.addCartItem(cartCommand2);
        CreateOrderCommand orderCommand2 = new CreateOrderCommand(user2.getUserId(), "김철수", "서울시 서초구", null);
        CreateOrderResult createResult2 = orderService.createOrder(orderCommand2);
        Integer orderId2 = createResult2.orderId();

        // when
        OrderResult result1 = orderService.getOrder(orderId1);
        OrderResult result2 = orderService.getOrder(orderId2);

        // then - 각 주문 데이터가 올바르게 조회됨
        assertThat(result1.orderId()).isEqualTo(orderId1);
        assertThat(result2.orderId()).isEqualTo(orderId2);
        assertThat(result1.orderId()).isNotEqualTo(result2.orderId());
        assertThat(result1.userId()).isEqualTo(user1.getUserId());
        assertThat(result2.userId()).isEqualTo(user2.getUserId());
    }

    @Test
    @Transactional
    @DisplayName("주문 조회 결과가 올바른 데이터를 포함해야 한다")
    void getOrder_ShouldReturnValidData() {
        // given
        User user = userRepository.findAll().stream().findFirst().orElseThrow();
        Product product = productRepository.findAll().stream().findFirst().orElseThrow();

        AddCartItemCommand cartCommand = new AddCartItemCommand(user.getUserId(), product.getProductId(), 2);
        cartService.addCartItem(cartCommand);
        CreateOrderCommand orderCommand = new CreateOrderCommand(user.getUserId(), "홍길동", "서울시 강남구", null);
        CreateOrderResult createResult = orderService.createOrder(orderCommand);
        Integer orderId = createResult.orderId();

        // when
        OrderResult order = orderService.getOrder(orderId);

        // then
        assertThat(order.orderId()).isEqualTo(orderId);
        assertThat(order.userId()).isEqualTo(user.getUserId());
        assertThat(order.deliveryUsername()).isEqualTo("홍길동");
        assertThat(order.deliveryAddress()).isEqualTo("서울시 강남구");
        assertThat(order.orderStatus()).isNotNull();
        assertThat(order.orderItems()).isNotEmpty();
    }
}