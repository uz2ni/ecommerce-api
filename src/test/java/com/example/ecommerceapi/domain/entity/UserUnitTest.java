package com.example.ecommerceapi.domain.entity;

import com.example.ecommerceapi.common.exception.ErrorCode;
import com.example.ecommerceapi.common.exception.PointException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("User 엔티티 단위 테스트")
class UserUnitTest {

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .userId(1)
                .username("testUser")
                .pointBalance(10000)
                .build();
    }

    @Nested
    @DisplayName("chargePoints 테스트")
    class ChargePointsTest {

        @Test
        @DisplayName("포인트 충전 시 잔액이 증가한다")
        void chargePoints_IncreasesBalance() {
            // given
            Integer chargeAmount = 5000;
            Integer initialBalance = user.getPointBalance();

            // when
            user.chargePoints(chargeAmount);

            // then
            assertEquals(initialBalance + chargeAmount, user.getPointBalance());
        }

        @Test
        @DisplayName("여러 번 충전 시 누적된다")
        void chargePoints_Accumulates() {
            // given
            Integer firstCharge = 3000;
            Integer secondCharge = 7000;
            Integer initialBalance = user.getPointBalance();

            // when
            user.chargePoints(firstCharge);
            user.chargePoints(secondCharge);

            // then
            assertEquals(initialBalance + firstCharge + secondCharge, user.getPointBalance());
        }
    }

    @Nested
    @DisplayName("usePoints 테스트")
    class UsePointsTest {

        @Test
        @DisplayName("포인트 사용 시 잔액이 감소한다")
        void usePoints_DecreasesBalance() {
            // given
            Integer useAmount = 5000;
            Integer initialBalance = user.getPointBalance();

            // when
            user.usePoints(useAmount);

            // then
            assertEquals(initialBalance - useAmount, user.getPointBalance());
        }

        @Test
        @DisplayName("잔액과 동일한 금액 사용 - 경계값")
        void usePoints_ExactBalance_Success() {
            // given
            Integer useAmount = 10000; // 현재 잔액과 동일
            Integer expectedBalance = 0;

            // when
            user.usePoints(useAmount);

            // then
            assertEquals(expectedBalance, user.getPointBalance());
        }

        @Test
        @DisplayName("잔액보다 1원 많은 금액 사용 시 예외 발생 - 경계값")
        void usePoints_OneMoreThanBalance_ThrowsException() {
            // given
            Integer useAmount = 10001; // 현재 잔액 + 1

            // when & then
            PointException exception = assertThrows(PointException.class,
                    () -> user.usePoints(useAmount));
            assertEquals(ErrorCode.POINT_INSUFFICIENT_BALANCE, exception.getErrorCode());
        }

        @Test
        @DisplayName("잔액보다 훨씬 많은 금액 사용 시 예외 발생")
        void usePoints_MuchMoreThanBalance_ThrowsException() {
            // given
            Integer useAmount = 50000;

            // when & then
            PointException exception = assertThrows(PointException.class,
                    () -> user.usePoints(useAmount));
            assertEquals(ErrorCode.POINT_INSUFFICIENT_BALANCE, exception.getErrorCode());
        }

        @Test
        @DisplayName("잔액 0원일 때 포인트 사용 시 예외 발생")
        void usePoints_ZeroBalance_ThrowsException() {
            // given
            user = User.builder()
                    .userId(1)
                    .username("testUser")
                    .pointBalance(0)
                    .build();
            Integer useAmount = 1000;

            // when & then
            PointException exception = assertThrows(PointException.class,
                    () -> user.usePoints(useAmount));
            assertEquals(ErrorCode.POINT_INSUFFICIENT_BALANCE, exception.getErrorCode());
        }
    }

    @Nested
    @DisplayName("refundPoints 테스트")
    class RefundPointsTest {

        @Test
        @DisplayName("포인트 환불 시 잔액이 증가한다")
        void refundPoints_IncreasesBalance() {
            // given
            Integer refundAmount = 3000;
            Integer initialBalance = user.getPointBalance();

            // when
            user.refundPoints(refundAmount);

            // then
            assertEquals(initialBalance + refundAmount, user.getPointBalance());
        }

        @Test
        @DisplayName("여러 번 환불 시 누적된다")
        void refundPoints_Accumulates() {
            // given
            Integer firstRefund = 2000;
            Integer secondRefund = 5000;
            Integer initialBalance = user.getPointBalance();

            // when
            user.refundPoints(firstRefund);
            user.refundPoints(secondRefund);

            // then
            assertEquals(initialBalance + firstRefund + secondRefund, user.getPointBalance());
        }
    }
}
