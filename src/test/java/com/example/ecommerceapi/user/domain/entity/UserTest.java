package com.example.ecommerceapi.user.domain.entity;

import com.example.ecommerceapi.common.exception.PointException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("User 엔티티 단위 테스트")
class UserTest {

    @Test
    @DisplayName("포인트 충전 시 잔액이 증가한다")
    void chargePoints_ShouldIncreaseBalance() {
        // given
        User user = User.builder()
                .userId(1)
                .username("testUser")
                .pointBalance(10000)
                .version(0)
                .build();

        // when
        user.chargePoints(5000);

        // then
        assertThat(user.getPointBalance()).isEqualTo(15000);
    }

    @Test
    @DisplayName("포인트 사용 시 잔액이 충분하면 감소한다")
    void usePoints_ShouldDecreaseBalance_WhenBalanceIsSufficient() {
        // given
        User user = User.builder()
                .userId(1)
                .username("testUser")
                .pointBalance(10000)
                .version(0)
                .build();

        // when
        user.usePoints(3000);

        // then
        assertThat(user.getPointBalance()).isEqualTo(7000);
    }

    @Test
    @DisplayName("포인트 사용 시 잔액이 부족하면 예외가 발생한다")
    void usePoints_ShouldThrowException_WhenBalanceIsInsufficient() {
        // given
        User user = User.builder()
                .userId(1)
                .username("testUser")
                .pointBalance(5000)
                .version(0)
                .build();

        // when & then
        assertThatThrownBy(() -> user.usePoints(10000))
                .isInstanceOf(PointException.class)
                .hasMessage("포인트 잔액이 부족합니다.");
    }

    @Test
    @DisplayName("포인트 환불 시 잔액이 증가한다")
    void refundPoints_ShouldIncreaseBalance() {
        // given
        User user = User.builder()
                .userId(1)
                .username("testUser")
                .pointBalance(10000)
                .version(0)
                .build();

        // when
        user.refundPoints(3000);

        // then
        assertThat(user.getPointBalance()).isEqualTo(13000);
    }

    @Test
    @DisplayName("포인트 사용 후 정확히 0원이 되면 성공한다")
    void usePoints_ShouldSucceed_WhenBalanceBecomesExactlyZero() {
        // given
        User user = User.builder()
                .userId(1)
                .username("testUser")
                .pointBalance(5000)
                .version(0)
                .build();

        // when
        user.usePoints(5000);

        // then
        assertThat(user.getPointBalance()).isEqualTo(0);
    }
}