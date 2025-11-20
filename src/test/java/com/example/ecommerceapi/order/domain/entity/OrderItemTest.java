package com.example.ecommerceapi.order.domain.entity;

import com.example.ecommerceapi.product.domain.entity.Product;
import com.example.ecommerceapi.user.domain.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("OrderItem 엔티티 단위 테스트")
class OrderItemTest {

    @Nested
    @DisplayName("주문 상품 생성 테스트")
    class CreateOrderItemTest {

        @Test
        @DisplayName("유효한 값으로 주문 상품을 생성한다")
        void createOrderItem_ShouldCreateOrderItem_WithValidValues() {
            // given
            Order order = Order.builder()
                    .orderId(1)
                    .user(User.builder().userId(1).version(0).build())
                    .build();
            Product product = Product.builder()
                    .productId(1)
                    .productName("테스트 상품")
                    .description("테스트 상품 설명")
                    .productPrice(10000)
                    .quantity(1000)
                    .build();
            Integer orderQuantity = 2;

            // when
            OrderItem orderItem = OrderItem.createOrderItem(
                    order, product, orderQuantity
            );

            // then
            assertThat(orderItem).isNotNull();
            assertThat(orderItem.getOrder()).isEqualTo(order);
            assertThat(orderItem.getProduct().getProductId()).isEqualTo(product.getProductId());
            assertThat(orderItem.getProduct().getProductName()).isEqualTo(product.getProductName());
            assertThat(orderItem.getProduct().getDescription()).isEqualTo(product.getDescription());
            assertThat(orderItem.getProduct().getProductPrice()).isEqualTo(product.getProductPrice());
            assertThat(orderItem.getOrderQuantity()).isEqualTo(orderQuantity);
            assertThat(orderItem.getTotalPrice()).isEqualTo(20000);
        }

        @Test
        @DisplayName("수량 1로 주문 상품을 생성한다")
        void createOrderItem_ShouldCreateOrderItem_WithQuantityOne() {
            // given
            Order order = Order.builder()
                    .orderId(1)
                    .user(User.builder().userId(1).version(0).build())
                    .build();
            Product product = Product.builder()
                    .productId(1)
                    .productName("상품명")
                    .description("상품설명")
                    .productPrice(10000)
                    .quantity(1000)
                    .build();

            // when
            OrderItem orderItem = OrderItem.createOrderItem(
                    order, product, 1
            );

            // then
            assertThat(orderItem.getOrderQuantity()).isEqualTo(1);
            assertThat(orderItem.getTotalPrice()).isEqualTo(10000);
        }

        @Test
        @DisplayName("총 금액이 정확히 계산된다")
        void createOrderItem_ShouldCalculateTotalPriceCorrectly() {
            // given
            Order order = Order.builder()
                    .orderId(1)
                    .user(User.builder().userId(1).version(0).build())
                    .build();
            Product product = Product.builder()
                    .productId(1)
                    .productName("상품명")
                    .description("상품설명")
                    .productPrice(15000)
                    .quantity(1000)
                    .build();
            Integer orderQuantity = 5;

            // when
            OrderItem orderItem = OrderItem.createOrderItem(
                    order, product, orderQuantity
            );

            // then
            assertThat(orderItem.getTotalPrice()).isEqualTo(75000);
        }

        @Test
        @DisplayName("여러 개의 주문 상품을 생성할 수 있다")
        void createOrderItem_ShouldCreateMultipleItems() {
            // given
            Order order = Order.builder()
                    .orderId(1)
                    .user(User.builder().userId(1).version(0).build())
                    .build();

            Product product1 = Product.builder()
                    .productId(1)
                    .productName("상품1")
                    .description("설명1")
                    .productPrice(10000)
                    .quantity(1000)
                    .build();
            Integer orderQuantity1 = 2;

            Product product2 = Product.builder()
                    .productId(1)
                    .productName("상품2")
                    .description("설명2")
                    .productPrice(20000)
                    .quantity(1000)
                    .build();
            Integer orderQuantity2 = 3;


            // when
            OrderItem orderItem1 = OrderItem.createOrderItem(
                    order, product1, orderQuantity1
            );
            OrderItem orderItem2 = OrderItem.createOrderItem(
                    order, product2, orderQuantity2
            );

            // then
            assertThat(orderItem1.getOrder()).isEqualTo(order);
            assertThat(orderItem1.getTotalPrice()).isEqualTo(20000);
            assertThat(orderItem2.getOrder()).isEqualTo(order);
            assertThat(orderItem2.getTotalPrice()).isEqualTo(60000);
        }

        @Test
        @DisplayName("같은 주문에 다른 상품을 추가할 수 있다")
        void createOrderItem_ShouldCreateItemsForSameOrder() {
            // given
            Order order = Order.builder()
                    .orderId(1)
                    .user(User.builder().userId(1).version(0).build())
                    .build();

            Product product1 = Product.builder()
                    .productId(1)
                    .productName("상품1")
                    .description("설명1")
                    .productPrice(10000)
                    .quantity(1000)
                    .build();
            Integer orderQuantity1 = 2;

            Product product2 = Product.builder()
                    .productId(2)
                    .productName("상품2")
                    .description("설명2")
                    .productPrice(20000)
                    .quantity(1000)
                    .build();
            Integer orderQuantity2 = 3;

            // when
            OrderItem orderItem1 = OrderItem.createOrderItem(
                    order, product1, orderQuantity1
            );
            OrderItem orderItem2 = OrderItem.createOrderItem(
                    order, product2, orderQuantity2
            );

            // then
            assertThat(orderItem1.getOrder()).isEqualTo(order);
            assertThat(orderItem2.getOrder()).isEqualTo(order);
            assertThat(orderItem1.getProduct().getProductId()).isEqualTo(1);
            assertThat(orderItem2.getProduct().getProductId()).isEqualTo(2);
        }

        @Test
        @DisplayName("큰 금액의 주문 상품을 생성한다")
        void createOrderItem_ShouldCreateOrderItem_WithLargeAmount() {
            // given
            Order order = Order.builder()
                    .orderId(1)
                    .user(User.builder().userId(1).version(0).build())
                    .build();

            Integer orderQuantity = 10;
            Integer productPrice = 1000000;
            Product product = Product.builder()
                    .productId(1)
                    .productName("고가 상품")
                    .description("설명")
                    .productPrice(productPrice)
                    .quantity(1000)
                    .build();

            // when
            OrderItem orderItem = OrderItem.createOrderItem(
                    order, product, orderQuantity
            );

            // then
            assertThat(orderItem.getTotalPrice()).isEqualTo(10000000);
        }
    }
}