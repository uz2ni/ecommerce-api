package com.example.ecommerceapi.coupon.application.service;

import com.example.ecommerceapi.common.exception.CouponException;
import com.example.ecommerceapi.common.exception.UserException;
import com.example.ecommerceapi.coupon.application.dto.CouponResult;
import com.example.ecommerceapi.coupon.application.dto.CouponUserResult;
import com.example.ecommerceapi.coupon.application.dto.IssueCouponCommand;
import com.example.ecommerceapi.coupon.application.dto.IssueCouponResult;
import com.example.ecommerceapi.coupon.application.validator.CouponValidator;
import com.example.ecommerceapi.coupon.domain.entity.Coupon;
import com.example.ecommerceapi.coupon.domain.entity.CouponUser;
import com.example.ecommerceapi.coupon.domain.repository.CouponRepository;
import com.example.ecommerceapi.coupon.domain.repository.CouponUserRepository;
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
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("CouponService 단위 테스트")
class CouponServiceTest {

    @Mock
    private CouponValidator couponValidator;

    @Mock
    private UserValidator userValidator;

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private CouponUserRepository couponUserRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CouponService couponService;

    private User user;
    private User user2;
    private Coupon availableCoupon;
    private Coupon expiredCoupon;
    private Coupon soldOutCoupon;
    private CouponUser couponUser1;
    private CouponUser couponUser2;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .userId(1)
                .username("테스트 사용자")
                .pointBalance(100000)
                .version(0)
                .build();

        user2 = User.builder()
                .userId(2)
                .username("테스트 사용자2")
                .pointBalance(200000)
                .version(0)
                .build();

        availableCoupon = Coupon.builder()
                .couponId(1)
                .couponName("할인 쿠폰")
                .discountAmount(10000)
                .totalQuantity(50)
                .issuedQuantity(10)
                .expiredAt(LocalDateTime.now().plusDays(30))
                .createdAt(LocalDateTime.now())
                .version(1)
                .build();

        expiredCoupon = Coupon.builder()
                .couponId(2)
                .couponName("만료된 쿠폰")
                .discountAmount(5000)
                .totalQuantity(50)
                .issuedQuantity(10)
                .expiredAt(LocalDateTime.now().minusDays(1))
                .createdAt(LocalDateTime.now().minusDays(30))
                .version(1)
                .build();

        soldOutCoupon = Coupon.builder()
                .couponId(3)
                .couponName("품절 쿠폰")
                .discountAmount(15000)
                .totalQuantity(10)
                .issuedQuantity(10)
                .expiredAt(LocalDateTime.now().plusDays(30))
                .createdAt(LocalDateTime.now())
                .version(1)
                .build();

        couponUser1 = CouponUser.builder()
                .couponUserId(1)
                .coupon(Coupon.builder().couponId(1).version(1).build())
                .user(user)
                .used(false)
                .issuedAt(LocalDateTime.now().minusDays(5))
                .version(1)
                .build();

        couponUser2 = CouponUser.builder()
                .couponUserId(2)
                .coupon(Coupon.builder().couponId(1).version(1).build())
                .user(user2)
                .used(true)
                .issuedAt(LocalDateTime.now().minusDays(4))
                .usedAt(LocalDateTime.now().minusDays(2))
                .version(1)
                .build();
    }

    @Nested
    @DisplayName("쿠폰 목록 조회 테스트")
    class GetAllCouponsTest {

        @Test
        @DisplayName("모든 쿠폰 목록을 조회한다")
        void getAllCoupons_ShouldReturnAllCoupons() {
            // given
            List<Coupon> coupons = Arrays.asList(availableCoupon, expiredCoupon, soldOutCoupon);
            given(couponRepository.findAll()).willReturn(coupons);

            // when
            List<CouponResult> result = couponService.getAllCoupons();

            // then
            assertThat(result).hasSize(3);
            assertThat(result.get(0).couponName()).isEqualTo("할인 쿠폰");
            assertThat(result.get(1).couponName()).isEqualTo("만료된 쿠폰");
            assertThat(result.get(2).couponName()).isEqualTo("품절 쿠폰");
            verify(couponRepository).findAll();
        }

        @Test
        @DisplayName("쿠폰이 없으면 빈 목록을 반환한다")
        void getAllCoupons_ShouldReturnEmptyList_WhenNoCoupons() {
            // given
            given(couponRepository.findAll()).willReturn(Arrays.asList());

            // when
            List<CouponResult> result = couponService.getAllCoupons();

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("쿠폰 발급 테스트")
    class IssueCouponTest {

        @Test
        @DisplayName("유효한 쿠폰을 발급한다")
        void issueCoupon_ShouldIssueCoupon_WhenValid() {
            // given
            IssueCouponCommand command = new IssueCouponCommand(
                    1,
                    1
            );

            given(userValidator.validateAndGetUser(1)).willReturn(user);
            given(couponRepository.findById(1)).willReturn(Optional.of(availableCoupon));
            given(couponUserRepository.findByCouponIdAndUserId(1, 1)).willReturn(Optional.empty());
            given(couponRepository.save(availableCoupon)).willReturn(availableCoupon);
            given(couponUserRepository.save(any(CouponUser.class))).willAnswer(invocation -> {
                CouponUser cu = invocation.getArgument(0);
                cu.setCouponUserId(1);
                return cu;
            });

            // when
            IssueCouponResult result = couponService.issueCoupon(command);

            // then
            assertThat(result).isNotNull();
            assertThat(result.couponUserId()).isEqualTo(1);
            assertThat(result.couponId()).isEqualTo(1);
            assertThat(result.userId()).isEqualTo(1);
            assertThat(availableCoupon.getIssuedQuantity()).isEqualTo(11);
            verify(couponRepository).save(availableCoupon);
            verify(couponUserRepository).save(any(CouponUser.class));
        }

        @Test
        @DisplayName("존재하지 않는 사용자로 발급 시 예외가 발생한다")
        void issueCoupon_ShouldThrowException_WhenUserNotExists() {
            // given
            IssueCouponCommand command = new IssueCouponCommand(
                    999,
                    1
            );

            given(userValidator.validateAndGetUser(999))
                    .willThrow(new UserException(com.example.ecommerceapi.common.exception.ErrorCode.USER_NOT_FOUND));

            // when & then
            assertThatThrownBy(() -> couponService.issueCoupon(command))
                    .isInstanceOf(UserException.class)
                    .hasMessage("회원이 존재하지 않습니다.");
        }

        @Test
        @DisplayName("존재하지 않는 쿠폰으로 발급 시 예외가 발생한다")
        void issueCoupon_ShouldThrowException_WhenCouponNotExists() {
            // given
            IssueCouponCommand command = new IssueCouponCommand(
                    1,
                    999
            );

            given(userValidator.validateAndGetUser(1)).willReturn(user);
            given(couponRepository.findById(999)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> couponService.issueCoupon(command))
                    .isInstanceOf(CouponException.class)
                    .hasMessage("존재하지 않는 쿠폰입니다.");
        }

        @Test
        @DisplayName("이미 발급받은 쿠폰은 중복 발급할 수 없다")
        void issueCoupon_ShouldThrowException_WhenAlreadyIssued() {
            // given
            IssueCouponCommand command = new IssueCouponCommand(
                    1,
                    1
            );

            given(userValidator.validateAndGetUser(1)).willReturn(user);
            given(couponRepository.findById(1)).willReturn(Optional.of(availableCoupon));
            given(couponUserRepository.findByCouponIdAndUserId(1, 1)).willReturn(Optional.of(couponUser1));

            // when & then
            assertThatThrownBy(() -> couponService.issueCoupon(command))
                    .isInstanceOf(CouponException.class)
                    .hasMessage("이미 발급받은 쿠폰입니다.");
        }

        @Test
        @DisplayName("만료된 쿠폰은 발급할 수 없다")
        void issueCoupon_ShouldThrowException_WhenCouponExpired() {
            // given
            IssueCouponCommand command = new IssueCouponCommand(
                    1,
                    2
            );

            given(userValidator.validateAndGetUser(1)).willReturn(user);
            given(couponRepository.findById(2)).willReturn(Optional.of(expiredCoupon));
            given(couponUserRepository.findByCouponIdAndUserId(2, 1)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> couponService.issueCoupon(command))
                    .isInstanceOf(CouponException.class)
                    .hasMessage("발급 가능한 쿠폰이 아닙니다. 수량이 소진되었거나 만료되었습니다.");
        }

        @Test
        @DisplayName("품절된 쿠폰은 발급할 수 없다")
        void issueCoupon_ShouldThrowException_WhenCouponSoldOut() {
            // given
            IssueCouponCommand command = new IssueCouponCommand(
                    1,
                    3
            );

            given(userValidator.validateAndGetUser(1)).willReturn(user);
            given(couponRepository.findById(3)).willReturn(Optional.of(soldOutCoupon));
            given(couponUserRepository.findByCouponIdAndUserId(3, 1)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> couponService.issueCoupon(command))
                    .isInstanceOf(CouponException.class)
                    .hasMessage("발급 가능한 쿠폰이 아닙니다. 수량이 소진되었거나 만료되었습니다.");
        }

        @Test
        @DisplayName("마지막 쿠폰을 발급할 수 있다")
        void issueCoupon_ShouldIssueLastCoupon() {
            // given
            Coupon lastCoupon = Coupon.builder()
                    .couponId(4)
                    .couponName("마지막 쿠폰")
                    .discountAmount(10000)
                    .totalQuantity(10)
                    .issuedQuantity(9)
                    .expiredAt(LocalDateTime.now().plusDays(30))
                    .createdAt(LocalDateTime.now())
                    .version(1)
                    .build();

            IssueCouponCommand command = new IssueCouponCommand(
                    1,
                    4
            );

            given(userValidator.validateAndGetUser(1)).willReturn(user);
            given(couponRepository.findById(4)).willReturn(Optional.of(lastCoupon));
            given(couponUserRepository.findByCouponIdAndUserId(4, 1)).willReturn(Optional.empty());
            given(couponRepository.save(lastCoupon)).willReturn(lastCoupon);
            given(couponUserRepository.save(any(CouponUser.class))).willAnswer(invocation -> {
                CouponUser cu = invocation.getArgument(0);
                cu.setCouponUserId(10);
                return cu;
            });

            // when
            IssueCouponResult result = couponService.issueCoupon(command);

            // then
            assertThat(result).isNotNull();
            assertThat(lastCoupon.getIssuedQuantity()).isEqualTo(10);
            assertThat(lastCoupon.getRemainingQuantity()).isEqualTo(0);
            assertThat(lastCoupon.isAvailable()).isFalse();
        }
    }

    @Nested
    @DisplayName("쿠폰 사용 이력 조회 테스트")
    class GetCouponUsageHistoryTest {

        @Test
        @DisplayName("쿠폰 사용 이력을 조회한다")
        void getCouponUsageHistory_ShouldReturnUsageHistory() {
            // given
            List<CouponUser> couponUsers = Arrays.asList(couponUser1, couponUser2);
            User user1 = User.builder().userId(1).username("사용자1").version(0).build();
            User user2 = User.builder().userId(2).username("사용자2").version(0).build();

            given(couponValidator.validateAndGetCoupon(1)).willReturn(availableCoupon);
            given(couponUserRepository.findByCouponId(1)).willReturn(couponUsers);
            given(userRepository.findById(1)).willReturn(user1);
            given(userRepository.findById(2)).willReturn(user2);

            // when
            List<CouponUserResult> result = couponService.getCouponUsageHistory(1);

            // then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).userName()).isEqualTo("사용자1");
            assertThat(result.get(0).used()).isFalse();
            assertThat(result.get(1).userName()).isEqualTo("사용자2");
            assertThat(result.get(1).used()).isTrue();
            verify(couponValidator).validateAndGetCoupon(1);
            verify(couponUserRepository).findByCouponId(1);
        }

        @Test
        @DisplayName("쿠폰 사용 이력이 없으면 빈 목록을 반환한다")
        void getCouponUsageHistory_ShouldReturnEmptyList_WhenNoHistory() {
            // given
            given(couponValidator.validateAndGetCoupon(1)).willReturn(availableCoupon);
            given(couponUserRepository.findByCouponId(1)).willReturn(Arrays.asList());

            // when
            List<CouponUserResult> result = couponService.getCouponUsageHistory(1);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("존재하지 않는 쿠폰 ID로 조회 시 예외가 발생한다")
        void getCouponUsageHistory_ShouldThrowException_WhenCouponNotFound() {
            // given
            given(couponValidator.validateAndGetCoupon(999))
                    .willThrow(new CouponException(com.example.ecommerceapi.common.exception.ErrorCode.COUPON_NOT_FOUND));

            // when & then
            assertThatThrownBy(() -> couponService.getCouponUsageHistory(999))
                    .isInstanceOf(CouponException.class)
                    .hasMessage("존재하지 않는 쿠폰입니다.");
        }
    }
}