package com.example.ecommerceapi.point.application.service;

import com.example.ecommerceapi.common.exception.PointException;
import com.example.ecommerceapi.common.exception.UserException;
import com.example.ecommerceapi.point.application.dto.PointResult;
import com.example.ecommerceapi.point.domain.entity.Point;
import com.example.ecommerceapi.point.domain.entity.PointType;
import com.example.ecommerceapi.point.domain.repository.PointRepository;
import com.example.ecommerceapi.user.application.validator.UserValidator;
import com.example.ecommerceapi.user.domain.entity.User;
import com.example.ecommerceapi.user.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("PointService 단위 테스트")
class PointServiceTest {

    @Mock
    private PointRepository pointRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserValidator userValidator;

    @InjectMocks
    private PointService pointService;

    private User testUser;
    private Point chargePoint;
    private Point usePoint;

    @BeforeEach
    void setUp() {
        // properties 값 주입
        ReflectionTestUtils.setField(pointService, "minAmount", 1000);
        ReflectionTestUtils.setField(pointService, "maxAmount", 1000000);

        testUser = User.builder()
                .userId(1)
                .username("testUser")
                .pointBalance(10000)
                .build();

        chargePoint = Point.builder()
                .pointId(1)
                .user(testUser)
                .pointType(PointType.CHARGE)
                .pointAmount(5000)
                .createdAt(LocalDateTime.now())
                .build();

        usePoint = Point.builder()
                .pointId(2)
                .user(testUser)
                .pointType(PointType.USE)
                .pointAmount(3000)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("사용자의 포인트 이력을 조회한다")
    void getPointHistory_ShouldReturnHistory_WhenUserExists() {
        // given
        List<Point> points = Arrays.asList(chargePoint, usePoint);
        given(userValidator.validateAndGetUser(1)).willReturn(testUser);
        given(pointRepository.findAllByUserId(1)).willReturn(points);

        // when
        List<PointResult> result = pointService.getPointHistory(1);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).pointType()).isEqualTo(PointType.CHARGE.name());
        assertThat(result.get(0).pointAmount()).isEqualTo(5000);
        assertThat(result.get(1).pointType()).isEqualTo(PointType.USE.name());
        assertThat(result.get(1).pointAmount()).isEqualTo(3000);
    }

    @Test
    @DisplayName("존재하지 않는 사용자의 포인트 이력 조회 시 예외가 발생한다")
    void getPointHistory_ShouldThrowException_WhenUserNotFound() {
        // given
        willThrow(new UserException(com.example.ecommerceapi.common.exception.ErrorCode.USER_NOT_FOUND))
                .given(userValidator).validateAndGetUser(999);

        // when & then
        assertThatThrownBy(() -> pointService.getPointHistory(999))
                .isInstanceOf(UserException.class)
                .hasMessage("회원이 존재하지 않습니다.");
    }

    @Test
    @DisplayName("포인트 이력이 없는 사용자는 빈 목록을 반환한다")
    void getPointHistory_ShouldReturnEmptyList_WhenNoHistory() {
        // given
        given(userValidator.validateAndGetUser(1)).willReturn(testUser);
        given(pointRepository.findAllByUserId(1)).willReturn(Arrays.asList());

        // when
        List<PointResult> result = pointService.getPointHistory(1);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("포인트 충전에 성공한다")
    void chargePoint_ShouldSucceed_WhenValidRequest() {
        // given
        Integer amount = 5000;
        Integer beforeBalance = testUser.getPointBalance();
        Point savedPoint = Point.builder()
                .pointId(1)
                .user(testUser)
                .pointType(PointType.CHARGE)
                .pointAmount(amount)
                .createdAt(LocalDateTime.now())
                .build();

        given(userValidator.validateAndGetUserWithLock(1)).willReturn(testUser);
        given(userRepository.save(any(User.class))).willReturn(testUser);
        given(pointRepository.save(any(Point.class))).willReturn(savedPoint);

        // when
        PointResult result = pointService.chargePoint(1, amount);

        // then
        assertThat(result.pointId()).isEqualTo(1);
        assertThat(result.userId()).isEqualTo(1);
        assertThat(result.pointType()).isEqualTo(PointType.CHARGE.name());
        assertThat(result.pointAmount()).isEqualTo(amount);

        verify(userRepository).save(any(User.class));
        verify(pointRepository).save(any(Point.class));
    }

    @Test
    @DisplayName("포인트 충전 시 금액이 null이면 예외가 발생한다")
    void chargePoint_ShouldThrowException_WhenAmountIsNull() {
        // when & then
        assertThatThrownBy(() -> pointService.chargePoint(1, null))
                .isInstanceOf(PointException.class)
                .hasMessage("포인트 금액이 유효하지 않습니다.");
    }

    @Test
    @DisplayName("포인트 충전 시 금액이 최소값보다 작으면 예외가 발생한다")
    void chargePoint_ShouldThrowException_WhenAmountBelowMinimum() {
        // given
        Integer invalidAmount = 500;

        // when & then
        assertThatThrownBy(() -> pointService.chargePoint(1, invalidAmount))
                .isInstanceOf(PointException.class)
                .hasMessage("포인트 금액이 유효하지 않습니다.");
    }

    @Test
    @DisplayName("포인트 충전 시 금액이 최대값보다 크면 예외가 발생한다")
    void chargePoint_ShouldThrowException_WhenAmountAboveMaximum() {
        // given
        Integer invalidAmount = 1500000;

        // when & then
        assertThatThrownBy(() -> pointService.chargePoint(1, invalidAmount))
                .isInstanceOf(PointException.class)
                .hasMessage("포인트 금액이 유효하지 않습니다.");
    }

    @Test
    @DisplayName("존재하지 않는 사용자에게 포인트 충전 시 예외가 발생한다")
    void chargePoint_ShouldThrowException_WhenUserNotFound() {
        // given
        Integer validAmount = 5000;
        willThrow(new UserException(com.example.ecommerceapi.common.exception.ErrorCode.USER_NOT_FOUND))
                .given(userValidator).validateAndGetUserWithLock(999);

        // when & then
        assertThatThrownBy(() -> pointService.chargePoint(999, validAmount))
                .isInstanceOf(UserException.class)
                .hasMessage("회원이 존재하지 않습니다.");
    }

    @Test
    @DisplayName("포인트 충전 시 최소값과 같은 금액은 성공한다")
    void chargePoint_ShouldSucceed_WhenAmountEqualsMinimum() {
        // given
        Integer minAmount = 1000;
        Point savedPoint = Point.builder()
                .pointId(1)
                .user(testUser)
                .pointType(PointType.CHARGE)
                .pointAmount(minAmount)
                .createdAt(LocalDateTime.now())
                .build();

        given(userValidator.validateAndGetUserWithLock(1)).willReturn(testUser);
        given(pointRepository.save(any(Point.class))).willReturn(savedPoint);

        // when
        PointResult result = pointService.chargePoint(1, minAmount);

        // then
        assertThat(result.pointAmount()).isEqualTo(1000);
    }

    @Test
    @DisplayName("포인트 충전 시 최대값과 같은 금액은 성공한다")
    void chargePoint_ShouldSucceed_WhenAmountEqualsMaximum() {
        // given
        Integer maxAmount = 1000000;
        Point savedPoint = Point.builder()
                .pointId(1)
                .user(testUser)
                .pointType(PointType.CHARGE)
                .pointAmount(maxAmount)
                .createdAt(LocalDateTime.now())
                .build();

        given(userValidator.validateAndGetUserWithLock(1)).willReturn(testUser);
        given(pointRepository.save(any(Point.class))).willReturn(savedPoint);

        // when
        PointResult result = pointService.chargePoint(1, maxAmount);

        // then
        assertThat(result.pointAmount()).isEqualTo(1000000);
    }
}