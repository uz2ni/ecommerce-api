package com.example.ecommerceapi.order.domain.entity;

import com.example.ecommerceapi.common.exception.OrderException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Order 엔티티 단위 테스트")
class OrderTest {

    @Nested
    @DisplayName("주문 생성 테스트")
    class CreateOrderTest {

        @Test
        @DisplayName("유효한 값으로 주문을 생성한다")
        void createOrder_ShouldCreateOrder_WithValidValues() {
            // given
            Integer userId = 1;
            String deliveryUsername = "홍길동";
            String deliveryAddress = "서울시 강남구";
            Integer totalOrderAmount = 50000;
            Integer discountAmount = 5000;
            Integer couponId = 1;

            // when
            Order order = Order.createOrder(
                    userId, deliveryUsername, deliveryAddress,
                    totalOrderAmount, discountAmount, couponId
            );

            // then
            assertThat(order).isNotNull();
            assertThat(order.getUserId()).isEqualTo(userId);
            assertThat(order.getDeliveryUsername()).isEqualTo(deliveryUsername);
            assertThat(order.getDeliveryAddress()).isEqualTo(deliveryAddress);
            assertThat(order.getTotalOrderAmount()).isEqualTo(totalOrderAmount);
            assertThat(order.getTotalDiscountAmount()).isEqualTo(discountAmount);
            assertThat(order.getFinalPaymentAmount()).isEqualTo(45000);
            assertThat(order.getCouponId()).isEqualTo(couponId);
            assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.PENDING);
            assertThat(order.getUsedPoint()).isEqualTo(0);
            assertThat(order.getCreatedAt()).isNotNull();
            assertThat(order.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("할인 없이 주문을 생성한다")
        void createOrder_ShouldCreateOrder_WithoutDiscount() {
            // given
            Integer totalOrderAmount = 50000;
            Integer discountAmount = 0;

            // when
            Order order = Order.createOrder(
                    1, "홍길동", "서울시 강남구",
                    totalOrderAmount, discountAmount, null
            );

            // then
            assertThat(order.getTotalOrderAmount()).isEqualTo(totalOrderAmount);
            assertThat(order.getTotalDiscountAmount()).isEqualTo(0);
            assertThat(order.getFinalPaymentAmount()).isEqualTo(50000);
            assertThat(order.getCouponId()).isNull();
        }

        @Test
        @DisplayName("최종 결제 금액이 정확히 계산된다")
        void createOrder_ShouldCalculateFinalPaymentAmountCorrectly() {
            // given
            Integer totalOrderAmount = 100000;
            Integer discountAmount = 30000;

            // when
            Order order = Order.createOrder(
                    1, "홍길동", "서울시 강남구",
                    totalOrderAmount, discountAmount, 1
            );

            // then
            assertThat(order.getFinalPaymentAmount()).isEqualTo(70000);
        }

        @Test
        @DisplayName("주문 생성 시 초기 상태는 PENDING이다")
        void createOrder_ShouldHavePendingStatus() {
            // when
            Order order = Order.createOrder(
                    1, "홍길동", "서울시 강남구",
                    50000, 0, null
            );

            // then
            assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.PENDING);
        }
    }

    @Nested
    @DisplayName("결제 완료 처리 테스트")
    class CompletePaymentTest {

        @Test
        @DisplayName("PENDING 상태의 주문을 결제 완료 처리한다")
        void completePayment_ShouldChangeStatusToPaid_WhenPending() {
            // given
            Order order = Order.createOrder(
                    1, "홍길동", "서울시 강남구",
                    50000, 0, null
            );
            assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.PENDING);

            // when
            order.completePayment();

            // then
            assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.PAID);
            assertThat(order.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("이미 결제 완료된 주문은 예외가 발생한다")
        void completePayment_ShouldThrowException_WhenAlreadyPaid() {
            // given
            Order order = Order.createOrder(
                    1, "홍길동", "서울시 강남구",
                    50000, 0, null
            );
            order.completePayment();
            assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.PAID);

            // when & then
            assertThatThrownBy(() -> order.completePayment())
                    .isInstanceOf(OrderException.class)
                    .hasMessage("이미 결제된 주문입니다.");
        }
    }

    @Nested
    @DisplayName("주문 취소 테스트")
    class CancelTest {

        @Test
        @DisplayName("주문을 취소한다")
        void cancel_ShouldChangeStatusToCancelled() {
            // given
            Order order = Order.createOrder(
                    1, "홍길동", "서울시 강남구",
                    50000, 0, null
            );

            // when
            order.cancel();

            // then
            assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.CANCELLED);
            assertThat(order.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("PAID 상태의 주문도 취소할 수 있다")
        void cancel_ShouldCancelPaidOrder() {
            // given
            Order order = Order.createOrder(
                    1, "홍길동", "서울시 강남구",
                    50000, 0, null
            );
            order.completePayment();
            assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.PAID);

            // when
            order.cancel();

            // then
            assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.CANCELLED);
        }
    }

    @Nested
    @DisplayName("결제 가능 상태 검증 테스트")
    class ValidatePaymentAvailableTest {

        @Test
        @DisplayName("PENDING 상태는 결제 가능하다")
        void validatePaymentAvailable_ShouldPass_WhenPending() {
            // given
            Order order = Order.createOrder(
                    1, "홍길동", "서울시 강남구",
                    50000, 0, null
            );

            // when & then
            assertThatCode(() -> order.validatePaymentAvailable())
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("PAID 상태는 결제 불가능하다")
        void validatePaymentAvailable_ShouldThrowException_WhenPaid() {
            // given
            Order order = Order.createOrder(
                    1, "홍길동", "서울시 강남구",
                    50000, 0, null
            );
            order.completePayment();

            // when & then
            assertThatThrownBy(() -> order.validatePaymentAvailable())
                    .isInstanceOf(OrderException.class)
                    .hasMessage("결제 가능한 주문 상태가 아닙니다.");
        }

        @Test
        @DisplayName("CANCELLED 상태는 결제 불가능하다")
        void validatePaymentAvailable_ShouldThrowException_WhenCancelled() {
            // given
            Order order = Order.createOrder(
                    1, "홍길동", "서울시 강남구",
                    50000, 0, null
            );
            order.cancel();

            // when & then
            assertThatThrownBy(() -> order.validatePaymentAvailable())
                    .isInstanceOf(OrderException.class)
                    .hasMessage("결제 가능한 주문 상태가 아닙니다.");
        }

        @Test
        @DisplayName("FAILED 상태는 결제 불가능하다")
        void validatePaymentAvailable_ShouldThrowException_WhenFailed() {
            // given
            Order order = Order.createOrder(
                    1, "홍길동", "서울시 강남구",
                    50000, 0, null
            );
            order.markPaymentFailed();

            // when & then
            assertThatThrownBy(() -> order.validatePaymentAvailable())
                    .isInstanceOf(OrderException.class)
                    .hasMessage("결제 가능한 주문 상태가 아닙니다.");
        }
    }

    @Nested
    @DisplayName("쿠폰 적용 여부 확인 테스트")
    class HasCouponTest {

        @Test
        @DisplayName("쿠폰이 적용된 주문은 true를 반환한다")
        void hasCoupon_ShouldReturnTrue_WhenCouponApplied() {
            // given
            Order order = Order.createOrder(
                    1, "홍길동", "서울시 강남구",
                    50000, 5000, 1
            );

            // when & then
            assertThat(order.hasCoupon()).isTrue();
        }

        @Test
        @DisplayName("쿠폰이 없는 주문은 false를 반환한다")
        void hasCoupon_ShouldReturnFalse_WhenNoCoupon() {
            // given
            Order order = Order.createOrder(
                    1, "홍길동", "서울시 강남구",
                    50000, 0, null
            );

            // when & then
            assertThat(order.hasCoupon()).isFalse();
        }
    }

    @Nested
    @DisplayName("결제 실패 처리 테스트")
    class MarkPaymentFailedTest {

        @Test
        @DisplayName("주문 상태를 FAILED로 변경한다")
        void markPaymentFailed_ShouldChangeStatusToFailed() {
            // given
            Order order = Order.createOrder(
                    1, "홍길동", "서울시 강남구",
                    50000, 0, null
            );

            // when
            order.markPaymentFailed();

            // then
            assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.FAILED);
        }
    }
}