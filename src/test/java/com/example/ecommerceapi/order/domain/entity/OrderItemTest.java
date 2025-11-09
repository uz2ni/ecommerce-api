package com.example.ecommerceapi.order.domain.entity;

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
            Integer orderId = 1;
            Integer productId = 1;
            String productName = "테스트 상품";
            String description = "테스트 상품 설명";
            Integer productPrice = 10000;
            Integer orderQuantity = 2;

            // when
            OrderItem orderItem = OrderItem.createOrderItem(
                    orderId, productId, productName, description,
                    productPrice, orderQuantity
            );

            // then
            assertThat(orderItem).isNotNull();
            assertThat(orderItem.getOrderId()).isEqualTo(orderId);
            assertThat(orderItem.getProductId()).isEqualTo(productId);
            assertThat(orderItem.getProductName()).isEqualTo(productName);
            assertThat(orderItem.getDescription()).isEqualTo(description);
            assertThat(orderItem.getProductPrice()).isEqualTo(productPrice);
            assertThat(orderItem.getOrderQuantity()).isEqualTo(orderQuantity);
            assertThat(orderItem.getTotalPrice()).isEqualTo(20000);
        }

        @Test
        @DisplayName("수량 1로 주문 상품을 생성한다")
        void createOrderItem_ShouldCreateOrderItem_WithQuantityOne() {
            // when
            OrderItem orderItem = OrderItem.createOrderItem(
                    1, 1, "상품", "설명", 10000, 1
            );

            // then
            assertThat(orderItem.getOrderQuantity()).isEqualTo(1);
            assertThat(orderItem.getTotalPrice()).isEqualTo(10000);
        }

        @Test
        @DisplayName("총 금액이 정확히 계산된다")
        void createOrderItem_ShouldCalculateTotalPriceCorrectly() {
            // given
            Integer productPrice = 15000;
            Integer orderQuantity = 5;

            // when
            OrderItem orderItem = OrderItem.createOrderItem(
                    1, 1, "상품", "설명", productPrice, orderQuantity
            );

            // then
            assertThat(orderItem.getTotalPrice()).isEqualTo(75000);
        }

        @Test
        @DisplayName("여러 개의 주문 상품을 생성할 수 있다")
        void createOrderItem_ShouldCreateMultipleItems() {
            // when
            OrderItem orderItem1 = OrderItem.createOrderItem(
                    1, 1, "상품1", "설명1", 10000, 2
            );
            OrderItem orderItem2 = OrderItem.createOrderItem(
                    1, 2, "상품2", "설명2", 20000, 3
            );

            // then
            assertThat(orderItem1.getOrderId()).isEqualTo(1);
            assertThat(orderItem1.getTotalPrice()).isEqualTo(20000);
            assertThat(orderItem2.getOrderId()).isEqualTo(1);
            assertThat(orderItem2.getTotalPrice()).isEqualTo(60000);
        }

        @Test
        @DisplayName("같은 주문에 다른 상품을 추가할 수 있다")
        void createOrderItem_ShouldCreateItemsForSameOrder() {
            // given
            Integer orderId = 1;

            // when
            OrderItem orderItem1 = OrderItem.createOrderItem(
                    orderId, 1, "상품1", "설명1", 10000, 1
            );
            OrderItem orderItem2 = OrderItem.createOrderItem(
                    orderId, 2, "상품2", "설명2", 20000, 1
            );

            // then
            assertThat(orderItem1.getOrderId()).isEqualTo(orderId);
            assertThat(orderItem2.getOrderId()).isEqualTo(orderId);
            assertThat(orderItem1.getProductId()).isEqualTo(1);
            assertThat(orderItem2.getProductId()).isEqualTo(2);
        }

        @Test
        @DisplayName("큰 금액의 주문 상품을 생성한다")
        void createOrderItem_ShouldCreateOrderItem_WithLargeAmount() {
            // given
            Integer productPrice = 1000000;
            Integer orderQuantity = 10;

            // when
            OrderItem orderItem = OrderItem.createOrderItem(
                    1, 1, "고가 상품", "설명", productPrice, orderQuantity
            );

            // then
            assertThat(orderItem.getTotalPrice()).isEqualTo(10000000);
        }
    }
}