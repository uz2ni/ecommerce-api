package com.example.ecommerceapi.product.application.service;

import com.example.ecommerceapi.common.exception.ErrorCode;
import com.example.ecommerceapi.common.exception.ProductException;
import com.example.ecommerceapi.order.domain.entity.Order;
import com.example.ecommerceapi.order.domain.entity.OrderItem;
import com.example.ecommerceapi.order.domain.entity.OrderStatus;
import com.example.ecommerceapi.order.domain.repository.OrderItemRepository;
import com.example.ecommerceapi.order.domain.repository.OrderRepository;
import com.example.ecommerceapi.product.application.dto.IncrementProductViewResult;
import com.example.ecommerceapi.product.application.dto.PopularProductResult;
import com.example.ecommerceapi.product.application.dto.ProductResult;
import com.example.ecommerceapi.product.application.dto.ProductStockResult;
import com.example.ecommerceapi.product.application.validator.ProductValidator;
import com.example.ecommerceapi.product.domain.entity.Product;
import com.example.ecommerceapi.product.domain.repository.ProductRepository;
import com.example.ecommerceapi.user.domain.entity.User;
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

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService 단위 테스트")
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductValidator productValidator;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @InjectMocks
    private ProductService productService;

    private Product product1;
    private Product product2;

    @BeforeEach
    void setUp() {
        product1 = Product.builder()
                .productId(1)
                .productName("상품1")
                .description("상품1 설명")
                .productPrice(10000)
                .quantity(50)
                .viewCount(100)
                .version(1)
                .build();

        product2 = Product.builder()
                .productId(2)
                .productName("상품2")
                .description("상품2 설명")
                .productPrice(20000)
                .quantity(30)
                .viewCount(200)
                .version(1)
                .build();
    }

    @Nested
    @DisplayName("상품 조회 테스트")
    class getProductTest {

        @Test
        @DisplayName("모든 상품 목록을 조회한다")
        void getAllProducts_ShouldReturnAllProducts() {
            // given
            List<Product> products = Arrays.asList(product1, product2);
            given(productRepository.findAll()).willReturn(products);

            // when
            List<ProductResult> result = productService.getAllProducts();

            // then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).productId()).isEqualTo(1);
            assertThat(result.get(0).productName()).isEqualTo("상품1");
            assertThat(result.get(0).productPrice()).isEqualTo(10000);
            assertThat(result.get(1).productId()).isEqualTo(2);
            assertThat(result.get(1).productName()).isEqualTo("상품2");
            assertThat(result.get(1).productPrice()).isEqualTo(20000);
        }

        @Test
        @DisplayName("상품이 없으면 빈 목록을 반환한다")
        void getAllProducts_ShouldReturnEmptyList_WhenNoProducts() {
            // given
            given(productRepository.findAll()).willReturn(Arrays.asList());

            // when
            List<ProductResult> result = productService.getAllProducts();

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("상품 ID로 상품을 조회한다")
        void getProduct_ShouldReturnProduct_WhenProductExists() {
            // given
            given(productValidator.validateAndGetProduct(1)).willReturn(product1);

            // when
            ProductResult result = productService.getProduct(1);

            // then
            assertThat(result.productId()).isEqualTo(1);
            assertThat(result.productName()).isEqualTo("상품1");
            assertThat(result.description()).isEqualTo("상품1 설명");
            assertThat(result.productPrice()).isEqualTo(10000);
        }

        @Test
        @DisplayName("존재하지 않는 상품을 조회하면 예외가 발생한다")
        void getProduct_ShouldThrowException_WhenProductNotFound() {
            // given
            given(productValidator.validateAndGetProduct(999)).willThrow(new ProductException(ErrorCode.PRODUCT_NOT_FOUND));

            // when & then
            assertThatThrownBy(() -> productService.getProduct(999))
                    .isInstanceOf(ProductException.class)
                    .hasMessage("존재하는 상품이 아닙니다.");
        }

    }

    @Nested
    @DisplayName("상품 재고 조회 테스트")
    class getProductStockTest {

        @Test
        @DisplayName("상품 재고를 조회한다")
        void getProductStock_ShouldReturnStock_WhenProductExists() {
            // given
            given(productValidator.validateAndGetProduct(1)).willReturn(product1);

            // when
            ProductStockResult result = productService.getProductStock(1);

            // then
            assertThat(result.stock()).isEqualTo(50);
        }

        @Test
        @DisplayName("존재하지 않는 상품의 재고 조회 시 예외가 발생한다")
        void getProductStock_ShouldThrowException_WhenProductNotFound() {
            // given
            given(productValidator.validateAndGetProduct(999)).willThrow(new ProductException(ErrorCode.PRODUCT_NOT_FOUND));

            // when & then
            assertThatThrownBy(() -> productService.getProductStock(999))
                    .isInstanceOf(ProductException.class)
                    .hasMessage("존재하는 상품이 아닙니다.");
        }
    }


    @Nested
    @DisplayName("인기 상품 조회 테스트")
    class getPopularProductsTest {

        @Test
        @DisplayName("조회수 기반 인기 상품을 조회한다")
        void getPopularProducts_ShouldReturnPopularProducts_ByViews() {
            // given
            List<Product> popularProducts = Arrays.asList(product2, product1);
            given(productRepository.findPopularProductsByView(5)).willReturn(popularProducts);

            // when
            List<PopularProductResult> result = productService.getPopularProducts("VIEWS", 3, 5);

            // then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).productId()).isEqualTo(2);
            assertThat(result.get(0).viewCount()).isEqualTo(200);
            assertThat(result.get(1).productId()).isEqualTo(1);
            assertThat(result.get(1).viewCount()).isEqualTo(100);
        }

        @Test
        @DisplayName("판매량 기반 인기 상품을 조회한다")
        void getPopularProducts_ShouldReturnPopularProducts_BySales() {
            // given
            LocalDateTime now = LocalDateTime.now();

            // 최근 결제 완료된 주문들 mock
            Order order1 = Order.builder()
                    .orderId(1)
                    .user(User.builder().userId(1).build())
                    .orderStatus(OrderStatus.PAID)
                    .createdAt(now.minusDays(1))
                    .build();
            Order order2 = Order.builder()
                    .orderId(2)
                    .user(User.builder().userId(1).build())
                    .orderStatus(OrderStatus.PAID)
                    .createdAt(now.minusDays(2))
                    .build();

            List<Order> orders = Arrays.asList(order1, order2);
            given(orderRepository.findAll()).willReturn(orders);

            // 주문 상품들 mock (product2: 50개, product1: 30개 판매)
            OrderItem orderItem1 = OrderItem.builder()
                    .orderItemId(1)
                    .order(Order.builder().orderId(1).build())
                    .product(product2)
                    .orderQuantity(30)
                    .build();
            OrderItem orderItem2 = OrderItem.builder()
                    .orderItemId(2)
                    .order(Order.builder().orderId(2).build())
                    .product(product2)
                    .orderQuantity(20)
                    .build();
            OrderItem orderItem3 = OrderItem.builder()
                    .orderItemId(3)
                    .order(Order.builder().orderId(1).build())
                    .product(product1)
                    .orderQuantity(30)
                    .build();

            List<OrderItem> orderItems = Arrays.asList(orderItem1, orderItem2, orderItem3);
            given(orderItemRepository.findAll()).willReturn(orderItems);

            // 상품 조회 mock
            given(productRepository.findById(2)).willReturn(product2);
            given(productRepository.findById(1)).willReturn(product1);

            // when
            List<PopularProductResult> result = productService.getPopularProducts("SALES", 3, 5);

            // then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).productId()).isEqualTo(2);
            assertThat(result.get(0).salesCount()).isEqualTo(50);
            assertThat(result.get(1).productId()).isEqualTo(1);
            assertThat(result.get(1).salesCount()).isEqualTo(30);
        }

        @Test
        @DisplayName("유효하지 않은 통계 타입으로 조회 시 예외가 발생한다")
        void getPopularProducts_ShouldThrowException_WhenInvalidType() {
            // when & then
            assertThatThrownBy(() -> productService.getPopularProducts("INVALID", 3, 5))
                    .isInstanceOf(ProductException.class)
                    .hasMessage("지원하지 않는 상품 통계 타입입니다");
        }

    }


    @Nested
    @DisplayName("상품 조회수 증가 테스트")
    class incrementProductViewCountTest {

        @Test
        @DisplayName("상품 조회수를 증가시킨다")
        void incrementProductViewCount_ShouldIncreaseViewCount_WhenProductExists() {
            // given
            given(productValidator.validateAndGetProduct(1)).willReturn(product1);
            Integer initialViewCount = product1.getViewCount();

            // when
            IncrementProductViewResult result = productService.incrementProductViewCount(1);

            // then
            assertThat(result.viewCount()).isEqualTo(initialViewCount + 1);
            assertThat(product1.getViewCount()).isEqualTo(initialViewCount + 1);
        }

        @Test
        @DisplayName("존재하지 않는 상품의 조회수 증가 시 예외가 발생한다")
        void incrementProductViewCount_ShouldThrowException_WhenProductNotFound() {
            // given
            given(productValidator.validateAndGetProduct(999)).willThrow(new ProductException(ErrorCode.PRODUCT_NOT_FOUND));

            // when & then
            assertThatThrownBy(() -> productService.incrementProductViewCount(999))
                    .isInstanceOf(ProductException.class)
                    .hasMessage("존재하는 상품이 아닙니다.");
        }
    }


}