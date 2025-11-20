package com.example.ecommerceapi.coupon.application.validator;

import com.example.ecommerceapi.common.exception.CouponException;
import com.example.ecommerceapi.coupon.domain.entity.Coupon;
import com.example.ecommerceapi.coupon.domain.repository.CouponRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("CouponValidator 단위 테스트")
class CouponValidatorTest {

    @Mock
    private CouponRepository couponRepository;

    @InjectMocks
    private CouponValidator couponValidator;

    private Coupon testCoupon;

    @BeforeEach
    void setUp() {
        testCoupon = Coupon.builder()
                .couponId(1)
                .couponName("테스트 쿠폰")
                .discountAmount(10000)
                .totalQuantity(50)
                .issuedQuantity(10)
                .expiredAt(LocalDateTime.now().plusDays(30))
                .createdAt(LocalDateTime.now())
                .version(1)
                .build();
    }

    @Test
    @DisplayName("쿠폰이 존재하면 Coupon 엔티티를 반환한다")
    void validateAndGetCoupon_ShouldReturnCoupon_WhenCouponExists() {
        // given
        given(couponRepository.findById(1)).willReturn(Optional.of(testCoupon));

        // when
        Coupon result = couponValidator.validateAndGetCoupon(1);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getCouponId()).isEqualTo(1);
        assertThat(result.getCouponName()).isEqualTo("테스트 쿠폰");
        assertThat(result.getDiscountAmount()).isEqualTo(10000);
        assertThat(result.getTotalQuantity()).isEqualTo(50);
        assertThat(result.getIssuedQuantity()).isEqualTo(10);
    }

    @Test
    @DisplayName("쿠폰이 존재하지 않으면 예외가 발생한다")
    void validateAndGetCoupon_ShouldThrowException_WhenCouponNotFound() {
        // given
        given(couponRepository.findById(999)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> couponValidator.validateAndGetCoupon(999))
                .isInstanceOf(CouponException.class)
                .hasMessage("존재하지 않는 쿠폰입니다.");
    }

    @Test
    @DisplayName("null ID로 조회하면 예외가 발생한다")
    void validateAndGetCoupon_ShouldThrowException_WhenIdIsNull() {
        // given
        given(couponRepository.findById(null)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> couponValidator.validateAndGetCoupon(null))
                .isInstanceOf(CouponException.class)
                .hasMessage("존재하지 않는 쿠폰입니다.");
    }
}