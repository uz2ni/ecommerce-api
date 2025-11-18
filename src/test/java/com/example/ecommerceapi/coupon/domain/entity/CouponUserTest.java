package com.example.ecommerceapi.coupon.domain.entity;

import com.example.ecommerceapi.user.domain.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("CouponUser 엔티티 단위 테스트")
class CouponUserTest {

    @Nested
    @DisplayName("쿠폰 발급 생성 테스트")
    class CreateIssuedCouponUserTest {

        @Test
        @DisplayName("유효한 값으로 쿠폰 발급 이력을 생성한다")
        void createIssuedCouponUser_ShouldCreateCouponUser_WithValidValues() {
            // given
            Coupon coupon = Coupon.builder().couponId(1).version(1).build();
            User user = User.builder().userId(1).build();

            // when
            CouponUser couponUser = CouponUser.createIssuedCouponUser(coupon, user);

            // then
            assertThat(couponUser).isNotNull();
            assertThat(couponUser.getCoupon()).isEqualTo(coupon);
            assertThat(couponUser.getUser()).isEqualTo(user);
            assertThat(couponUser.getUsed()).isFalse();
            assertThat(couponUser.getIssuedAt()).isNotNull();
            assertThat(couponUser.getUsedAt()).isNull();
        }

        @Test
        @DisplayName("생성된 쿠폰은 미사용 상태이다")
        void createIssuedCouponUser_ShouldBeUnused() {
            // given
            Coupon coupon = Coupon.builder().couponId(1).version(1).build();
            User user = User.builder().userId(1).build();

            // when
            CouponUser couponUser = CouponUser.createIssuedCouponUser(coupon, user);

            // then
            assertThat(couponUser.getUsed()).isFalse();
            assertThat(couponUser.getUsedAt()).isNull();
        }

        @Test
        @DisplayName("발급 시간은 현재 시간이다")
        void createIssuedCouponUser_ShouldSetIssuedAtToNow() {
            // given
            Coupon coupon = Coupon.builder().couponId(1).version(1).build();
            User user = User.builder().userId(1).build();
            LocalDateTime before = LocalDateTime.now().minusSeconds(1);

            // when
            CouponUser couponUser = CouponUser.createIssuedCouponUser(coupon, user);

            // then
            LocalDateTime after = LocalDateTime.now().plusSeconds(1);
            assertThat(couponUser.getIssuedAt()).isAfter(before);
            assertThat(couponUser.getIssuedAt()).isBefore(after);
        }
    }

    @Nested
    @DisplayName("쿠폰 사용 처리 테스트")
    class MarkAsUsedTest {

        @Test
        @DisplayName("쿠폰을 사용 처리하면 used가 true가 된다")
        void markAsUsed_ShouldSetUsedToTrue() {
            // given
            Coupon coupon = Coupon.builder().couponId(1).version(1).build();
            User user = User.builder().userId(1).build();
            CouponUser couponUser = CouponUser.createIssuedCouponUser(coupon, user);
            assertThat(couponUser.getUsed()).isFalse();

            // when
            couponUser.markAsUsed();

            // then
            assertThat(couponUser.getUsed()).isTrue();
        }

        @Test
        @DisplayName("쿠폰을 사용 처리하면 usedAt이 설정된다")
        void markAsUsed_ShouldSetUsedAt() {
            // given
            Coupon coupon = Coupon.builder().couponId(1).version(1).build();
            User user = User.builder().userId(1).build();
            CouponUser couponUser = CouponUser.createIssuedCouponUser(coupon, user);
            assertThat(couponUser.getUsedAt()).isNull();

            LocalDateTime before = LocalDateTime.now().minusSeconds(1);

            // when
            couponUser.markAsUsed();

            // then
            LocalDateTime after = LocalDateTime.now().plusSeconds(1);
            assertThat(couponUser.getUsedAt()).isNotNull();
            assertThat(couponUser.getUsedAt()).isAfter(before);
            assertThat(couponUser.getUsedAt()).isBefore(after);
        }

        @Test
        @DisplayName("이미 사용된 쿠폰도 다시 사용 처리할 수 있다")
        void markAsUsed_ShouldWorkOnAlreadyUsedCoupon() {
            // given
            Coupon coupon = Coupon.builder().couponId(1).version(1).build();
            User user = User.builder().userId(1).build();
            CouponUser couponUser = CouponUser.createIssuedCouponUser(coupon, user);
            couponUser.markAsUsed();
            LocalDateTime firstUsedAt = couponUser.getUsedAt();

            // when
            try {
                Thread.sleep(10); // usedAt 시간 차이를 만들기 위해
            } catch (InterruptedException e) {
                // ignore
            }
            couponUser.markAsUsed();

            // then
            assertThat(couponUser.getUsed()).isTrue();
            assertThat(couponUser.getUsedAt()).isNotNull();
            // 재사용 처리 시 usedAt이 업데이트됨
            assertThat(couponUser.getUsedAt()).isAfterOrEqualTo(firstUsedAt);
        }
    }

    @Nested
    @DisplayName("빌더 패턴 테스트")
    class BuilderTest {

        @Test
        @DisplayName("빌더로 쿠폰 발급 이력을 생성할 수 있다")
        void builder_ShouldCreateCouponUser() {
            // given
            Coupon coupon = Coupon.builder().couponId(10).version(1).build();
            User user = User.builder().userId(20).build();
            LocalDateTime now = LocalDateTime.now();

            // when
            CouponUser couponUser = CouponUser.builder()
                    .couponUserId(1)
                    .coupon(coupon)
                    .user(user)
                    .used(true)
                    .issuedAt(now)
                    .usedAt(now.plusDays(1))
                    .build();

            // then
            assertThat(couponUser.getCouponUserId()).isEqualTo(1);
            assertThat(couponUser.getCoupon().getCouponId()).isEqualTo(10);
            assertThat(couponUser.getUser().getUserId()).isEqualTo(20);
            assertThat(couponUser.getUsed()).isTrue();
            assertThat(couponUser.getIssuedAt()).isEqualTo(now);
            assertThat(couponUser.getUsedAt()).isEqualTo(now.plusDays(1));
        }

        @Test
        @DisplayName("빌더로 미사용 쿠폰을 생성할 수 있다")
        void builder_ShouldCreateUnusedCoupon() {
            // given
            Coupon coupon = Coupon.builder().couponId(1).version(1).build();
            User user = User.builder().userId(1).build();

            // when
            CouponUser couponUser = CouponUser.builder()
                    .coupon(coupon)
                    .user(user)
                    .used(false)
                    .issuedAt(LocalDateTime.now())
                    .build();

            // then
            assertThat(couponUser.getUsed()).isFalse();
            assertThat(couponUser.getUsedAt()).isNull();
        }
    }
}