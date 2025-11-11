package com.example.ecommerceapi.point.entity;

import com.example.ecommerceapi.common.exception.PointException;
import com.example.ecommerceapi.point.domain.entity.Point;
import com.example.ecommerceapi.point.domain.entity.PointType;
import com.example.ecommerceapi.user.domain.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Point 엔티티 단위 테스트")
class PointTest {

    @Test
    @DisplayName("포인트 충전 이력을 생성한다")
    void createChargeHistory_ShouldCreatePointWithChargeType() {
        // given
        User user = User.builder().userId(1).username("테스트유저").pointBalance(10000).build();
        Integer amount = 10000;
        LocalDateTime beforeCreation = LocalDateTime.now();

        // when
        Point point = Point.createChargeHistory(user, amount);

        // then
        assertThat(point.getUser()).isEqualTo(user);
        assertThat(point.getPointType()).isEqualTo(PointType.CHARGE);
        assertThat(point.getPointAmount()).isEqualTo(amount);
        assertThat(point.getCreatedAt()).isAfterOrEqualTo(beforeCreation);
        assertThat(point.getCreatedAt()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    @DisplayName("포인트 사용 이력을 생성한다")
    void createUseHistory_ShouldCreatePointWithUseType() {
        // given
        User user = User.builder().userId(1).username("테스트유저").pointBalance(10000).build();
        Integer amount = 5000;
        LocalDateTime beforeCreation = LocalDateTime.now();

        // when
        Point point = Point.createUseHistory(user, amount);

        // then
        assertThat(point.getUser()).isEqualTo(user);
        assertThat(point.getPointType()).isEqualTo(PointType.USE);
        assertThat(point.getPointAmount()).isEqualTo(amount);
        assertThat(point.getCreatedAt()).isAfterOrEqualTo(beforeCreation);
        assertThat(point.getCreatedAt()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    @DisplayName("포인트 환불 이력을 생성한다")
    void createRefundHistory_ShouldCreatePointWithRefundType() {
        // given
        User user = User.builder().userId(1).username("테스트유저").pointBalance(10000).build();
        Integer amount = 3000;
        LocalDateTime beforeCreation = LocalDateTime.now();

        // when
        Point point = Point.createRefundHistory(user, amount);

        // then
        assertThat(point.getUser()).isEqualTo(user);
        assertThat(point.getPointType()).isEqualTo(PointType.REFUND);
        assertThat(point.getPointAmount()).isEqualTo(amount);
        assertThat(point.getCreatedAt()).isAfterOrEqualTo(beforeCreation);
        assertThat(point.getCreatedAt()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    @DisplayName("포인트 금액이 유효한 범위일 때 검증을 통과한다")
    void validatePointAmount_ShouldPass_WhenAmountIsValid() {
        // given
        Integer validAmount = 5000;
        Integer minAmount = 1000;
        Integer maxAmount = 1000000;

        // when & then
        assertThatNoException().isThrownBy(() ->
                Point.validatePointAmount(validAmount, minAmount, maxAmount)
        );
    }

    @Test
    @DisplayName("포인트 금액이 null일 때 예외가 발생한다")
    void validatePointAmount_ShouldThrowException_WhenAmountIsNull() {
        // given
        Integer nullAmount = null;
        Integer minAmount = 1000;
        Integer maxAmount = 1000000;

        // when & then
        assertThatThrownBy(() ->
                Point.validatePointAmount(nullAmount, minAmount, maxAmount)
        ).isInstanceOf(PointException.class)
                .hasMessage("포인트 금액이 유효하지 않습니다.");
    }

    @Test
    @DisplayName("포인트 금액이 최소값보다 작을 때 예외가 발생한다")
    void validatePointAmount_ShouldThrowException_WhenAmountIsBelowMinimum() {
        // given
        Integer belowMinAmount = 500;
        Integer minAmount = 1000;
        Integer maxAmount = 1000000;

        // when & then
        assertThatThrownBy(() ->
                Point.validatePointAmount(belowMinAmount, minAmount, maxAmount)
        ).isInstanceOf(PointException.class)
                .hasMessage("포인트 금액이 유효하지 않습니다.");
    }

    @Test
    @DisplayName("포인트 금액이 최대값보다 클 때 예외가 발생한다")
    void validatePointAmount_ShouldThrowException_WhenAmountIsAboveMaximum() {
        // given
        Integer aboveMaxAmount = 1500000;
        Integer minAmount = 1000;
        Integer maxAmount = 1000000;

        // when & then
        assertThatThrownBy(() ->
                Point.validatePointAmount(aboveMaxAmount, minAmount, maxAmount)
        ).isInstanceOf(PointException.class)
                .hasMessage("포인트 금액이 유효하지 않습니다.");
    }

    @Test
    @DisplayName("포인트 금액이 최소값과 같을 때 검증을 통과한다")
    void validatePointAmount_ShouldPass_WhenAmountEqualsMinimum() {
        // given
        Integer minAmount = 1000;
        Integer maxAmount = 1000000;

        // when & then
        assertThatNoException().isThrownBy(() ->
                Point.validatePointAmount(minAmount, minAmount, maxAmount)
        );
    }

    @Test
    @DisplayName("포인트 금액이 최대값과 같을 때 검증을 통과한다")
    void validatePointAmount_ShouldPass_WhenAmountEqualsMaximum() {
        // given
        Integer maxAmount = 1000000;
        Integer minAmount = 1000;

        // when & then
        assertThatNoException().isThrownBy(() ->
                Point.validatePointAmount(maxAmount, minAmount, maxAmount)
        );
    }
}