package com.example.ecommerceapi.order.domain.entity;

import com.example.ecommerceapi.common.exception.OrderException;
import com.example.ecommerceapi.coupon.domain.entity.Coupon;
import com.example.ecommerceapi.user.domain.entity.User;
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
            User user = User.builder().userId(1).build();
            String deliveryUsername = "홍길동";
            String deliveryAddress = "서울시 강남구";
            Integer totalOrderAmount = 50000;
            Integer discountAmount = 5000;
            Coupon coupon = Coupon.builder().couponId(1).build();

            // when
            Order order = Order.createOrder(
                    user, deliveryUsername, deliveryAddress,
                    totalOrderAmount, discountAmount, coupon
            );

            // then
            assertThat(order).isNotNull();
            assertThat(order.getUser()).isEqualTo(user);
            assertThat(order.getDeliveryUsername()).isEqualTo(deliveryUsername);
            assertThat(order.getDeliveryAddress()).isEqualTo(deliveryAddress);
            assertThat(order.getTotalOrderAmount()).isEqualTo(totalOrderAmount);
            assertThat(order.getTotalDiscountAmount()).isEqualTo(discountAmount);
            assertThat(order.getFinalPaymentAmount()).isEqualTo(45000);
            assertThat(order.getCoupon()).isEqualTo(coupon);
            assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.PENDING);
            assertThat(order.getUsedPoint()).isEqualTo(0);
            assertThat(order.getCreatedAt()).isNotNull();
            assertThat(order.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("할인 없이 주문을 생성한다")
        void createOrder_ShouldCreateOrder_WithoutDiscount() {
            // given
            User user = User.builder().userId(1).build();
            Integer totalOrderAmount = 50000;
            Integer discountAmount = 0;

            // when
            Order order = Order.createOrder(
                    user, "홍길동", "서울시 강남구",
                    totalOrderAmount, discountAmount, null
            );

            // then
            assertThat(order.getTotalOrderAmount()).isEqualTo(totalOrderAmount);
            assertThat(order.getTotalDiscountAmount()).isEqualTo(0);
            assertThat(order.getFinalPaymentAmount()).isEqualTo(50000);
            assertThat(order.getCoupon()).isNull();
        }

        @Test
        @DisplayName("최종 결제 금액이 정확히 계산된다")
        void createOrder_ShouldCalculateFinalPaymentAmountCorrectly() {
            // given
            User user = User.builder().userId(1).build();
            Integer totalOrderAmount = 100000;
            Integer discountAmount = 30000;

            // when
            Order order = Order.createOrder(
                    user, "홍길동", "서울시 강남구",
                    totalOrderAmount, discountAmount, Coupon.builder().couponId(1).build()
            );

            // then
            assertThat(order.getFinalPaymentAmount()).isEqualTo(70000);
        }

        @Test
        @DisplayName("주문 생성 시 초기 상태는 PENDING이다")
        void createOrder_ShouldHavePendingStatus() {
            // given
            User user = User.builder().userId(1).build();

            // when
            Order order = Order.createOrder(
                    user, "홍길동", "서울시 강남구",
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
            User user = User.builder().userId(1).build();
            Order order = Order.createOrder(
                    user, "홍길동", "서울시 강남구",
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
            User user = User.builder().userId(1).build();
            Order order = Order.createOrder(
                    user, "홍길동", "서울시 강남구",
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
            User user = User.builder().userId(1).build();
            Order order = Order.createOrder(
                    user, "홍길동", "서울시 강남구",
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
            User user = User.builder().userId(1).build();
            Order order = Order.createOrder(
                    user, "홍길동", "서울시 강남구",
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
            User user = User.builder().userId(1).build();
            Order order = Order.createOrder(
                    user, "홍길동", "서울시 강남구",
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
            User user = User.builder().userId(1).build();
            Order order = Order.createOrder(
                    user, "홍길동", "서울시 강남구",
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
            User user = User.builder().userId(1).build();
            Order order = Order.createOrder(
                    user, "홍길동", "서울시 강남구",
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
            User user = User.builder().userId(1).build();
            Order order = Order.createOrder(
                    user, "홍길동", "서울시 강남구",
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
            User user = User.builder().userId(1).build();
            Order order = Order.createOrder(
                    user, "홍길동", "서울시 강남구",
                    50000, 5000, Coupon.builder().couponId(1).build()
            );

            // when & then
            assertThat(order.hasCoupon()).isTrue();
        }

        @Test
        @DisplayName("쿠폰이 없는 주문은 false를 반환한다")
        void hasCoupon_ShouldReturnFalse_WhenNoCoupon() {
            // given
            User user = User.builder().userId(1).build();
            Order order = Order.createOrder(
                    user, "홍길동", "서울시 강남구",
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
            User user = User.builder().userId(1).build();
            Order order = Order.createOrder(
                    user, "홍길동", "서울시 강남구",
                    50000, 0, null
            );

            // when
            order.markPaymentFailed();

            // then
            assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.FAILED);
        }
    }
}