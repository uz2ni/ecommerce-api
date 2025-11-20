package com.example.ecommerceapi.coupon.domain.entity;

import com.example.ecommerceapi.common.exception.CouponException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Coupon 엔티티 단위 테스트")
class CouponTest {

    @Nested
    @DisplayName("남은 수량 조회 테스트")
    class GetRemainingQuantityTest {

        @Test
        @DisplayName("남은 수량을 정확히 계산한다")
        void getRemainingQuantity_ShouldCalculateCorrectly() {
            // given
            Coupon coupon = Coupon.builder()
                    .totalQuantity(50)
                    .issuedQuantity(10)
                    .version(1)
                    .build();

            // when
            Integer remaining = coupon.getRemainingQuantity();

            // then
            assertThat(remaining).isEqualTo(40);
        }

        @Test
        @DisplayName("모두 발급된 경우 남은 수량은 0이다")
        void getRemainingQuantity_ShouldReturnZero_WhenAllIssued() {
            // given
            Coupon coupon = Coupon.builder()
                    .totalQuantity(10)
                    .issuedQuantity(10)
                    .version(1)
                    .build();

            // when
            Integer remaining = coupon.getRemainingQuantity();

            // then
            assertThat(remaining).isEqualTo(0);
        }

        @Test
        @DisplayName("발급되지 않은 경우 남은 수량은 전체 수량과 같다")
        void getRemainingQuantity_ShouldEqualTotalQuantity_WhenNoneIssued() {
            // given
            Coupon coupon = Coupon.builder()
                    .totalQuantity(100)
                    .issuedQuantity(0)
                    .version(1)
                    .build();

            // when
            Integer remaining = coupon.getRemainingQuantity();

            // then
            assertThat(remaining).isEqualTo(100);
        }
    }

    @Nested
    @DisplayName("쿠폰 만료 여부 테스트")
    class IsExpiredTest {

        @Test
        @DisplayName("만료일이 지나지 않은 쿠폰은 만료되지 않았다")
        void isExpired_ShouldReturnFalse_WhenNotExpired() {
            // given
            Coupon coupon = Coupon.builder()
                    .expiredAt(LocalDateTime.now().plusDays(10))
                    .version(1)
                    .build();

            // when
            boolean expired = coupon.isExpired();

            // then
            assertThat(expired).isFalse();
        }

        @Test
        @DisplayName("만료일이 지난 쿠폰은 만료되었다")
        void isExpired_ShouldReturnTrue_WhenExpired() {
            // given
            Coupon coupon = Coupon.builder()
                    .expiredAt(LocalDateTime.now().minusDays(1))
                    .version(1)
                    .build();

            // when
            boolean expired = coupon.isExpired();

            // then
            assertThat(expired).isTrue();
        }

        @Test
        @DisplayName("만료일이 null인 경우 만료되지 않았다")
        void isExpired_ShouldReturnFalse_WhenExpiredAtIsNull() {
            // given
            Coupon coupon = Coupon.builder()
                    .expiredAt(null)
                    .version(1)
                    .build();

            // when
            boolean expired = coupon.isExpired();

            // then
            assertThat(expired).isFalse();
        }
    }

    @Nested
    @DisplayName("쿠폰 발급 가능 여부 테스트")
    class IsAvailableTest {

        @Test
        @DisplayName("남은 수량이 있고 만료되지 않은 쿠폰은 발급 가능하다")
        void isAvailable_ShouldReturnTrue_WhenAvailable() {
            // given
            Coupon coupon = Coupon.builder()
                    .totalQuantity(50)
                    .issuedQuantity(10)
                    .expiredAt(LocalDateTime.now().plusDays(10))
                    .version(1)
                    .build();

            // when
            boolean available = coupon.isAvailable();

            // then
            assertThat(available).isTrue();
        }

        @Test
        @DisplayName("남은 수량이 0인 경우 발급 불가능하다")
        void isAvailable_ShouldReturnFalse_WhenNoRemainingQuantity() {
            // given
            Coupon coupon = Coupon.builder()
                    .totalQuantity(10)
                    .issuedQuantity(10)
                    .expiredAt(LocalDateTime.now().plusDays(10))
                    .version(1)
                    .build();

            // when
            boolean available = coupon.isAvailable();

            // then
            assertThat(available).isFalse();
        }

        @Test
        @DisplayName("만료된 쿠폰은 발급 불가능하다")
        void isAvailable_ShouldReturnFalse_WhenExpired() {
            // given
            Coupon coupon = Coupon.builder()
                    .totalQuantity(50)
                    .issuedQuantity(10)
                    .expiredAt(LocalDateTime.now().minusDays(1))
                    .version(1)
                    .build();

            // when
            boolean available = coupon.isAvailable();

            // then
            assertThat(available).isFalse();
        }

        @Test
        @DisplayName("남은 수량이 0이고 만료된 쿠폰은 발급 불가능하다")
        void isAvailable_ShouldReturnFalse_WhenNoQuantityAndExpired() {
            // given
            Coupon coupon = Coupon.builder()
                    .totalQuantity(10)
                    .issuedQuantity(10)
                    .expiredAt(LocalDateTime.now().minusDays(1))
                    .version(1)
                    .build();

            // when
            boolean available = coupon.isAvailable();

            // then
            assertThat(available).isFalse();
        }
    }

    @Nested
    @DisplayName("쿠폰 발급 테스트")
    class IssueCouponTest {

        @Test
        @DisplayName("발급 가능한 쿠폰은 발급 수량이 증가한다")
        void issueCoupon_ShouldIncreaseIssuedQuantity_WhenAvailable() {
            // given
            Coupon coupon = Coupon.builder()
                    .totalQuantity(50)
                    .issuedQuantity(10)
                    .expiredAt(LocalDateTime.now().plusDays(10))
                    .version(1)
                    .build();

            // when
            coupon.issueCoupon();

            // then
            assertThat(coupon.getIssuedQuantity()).isEqualTo(11);
            assertThat(coupon.getRemainingQuantity()).isEqualTo(39);
        }

        @Test
        @DisplayName("마지막 쿠폰을 발급할 수 있다")
        void issueCoupon_ShouldIssueLastCoupon() {
            // given
            Coupon coupon = Coupon.builder()
                    .totalQuantity(50)
                    .issuedQuantity(49)
                    .expiredAt(LocalDateTime.now().plusDays(10))
                    .version(1)
                    .build();

            // when
            coupon.issueCoupon();

            // then
            assertThat(coupon.getIssuedQuantity()).isEqualTo(50);
            assertThat(coupon.getRemainingQuantity()).isEqualTo(0);
            assertThat(coupon.isAvailable()).isFalse();
        }

        @Test
        @DisplayName("남은 수량이 0인 경우 예외가 발생한다")
        void issueCoupon_ShouldThrowException_WhenNoRemainingQuantity() {
            // given
            Coupon coupon = Coupon.builder()
                    .totalQuantity(10)
                    .issuedQuantity(10)
                    .expiredAt(LocalDateTime.now().plusDays(10))
                    .version(1)
                    .build();

            // when & then
            assertThatThrownBy(() -> coupon.issueCoupon())
                    .isInstanceOf(CouponException.class)
                    .hasMessage("발급 가능한 쿠폰이 아닙니다. 수량이 소진되었거나 만료되었습니다.");
        }

        @Test
        @DisplayName("만료된 쿠폰은 예외가 발생한다")
        void issueCoupon_ShouldThrowException_WhenExpired() {
            // given
            Coupon coupon = Coupon.builder()
                    .totalQuantity(50)
                    .issuedQuantity(10)
                    .expiredAt(LocalDateTime.now().minusDays(1))
                    .version(1)
                    .build();

            // when & then
            assertThatThrownBy(() -> coupon.issueCoupon())
                    .isInstanceOf(CouponException.class)
                    .hasMessage("발급 가능한 쿠폰이 아닙니다. 수량이 소진되었거나 만료되었습니다.");
        }

        @Test
        @DisplayName("여러 번 발급할 수 있다")
        void issueCoupon_ShouldIssueMultipleTimes() {
            // given
            Coupon coupon = Coupon.builder()
                    .totalQuantity(10)
                    .issuedQuantity(0)
                    .expiredAt(LocalDateTime.now().plusDays(10))
                    .version(1)
                    .build();

            // when
            coupon.issueCoupon();
            coupon.issueCoupon();
            coupon.issueCoupon();

            // then
            assertThat(coupon.getIssuedQuantity()).isEqualTo(3);
            assertThat(coupon.getRemainingQuantity()).isEqualTo(7);
            assertThat(coupon.isAvailable()).isTrue();
        }
    }
}