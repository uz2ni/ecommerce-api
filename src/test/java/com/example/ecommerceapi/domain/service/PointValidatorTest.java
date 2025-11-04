package com.example.ecommerceapi.domain.service;

import com.example.ecommerceapi.common.exception.ErrorCode;
import com.example.ecommerceapi.common.exception.PointException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PointValidator 테스트")
class PointValidatorTest {

    private PointValidator pointValidator;

    @BeforeEach
    void setUp() {
        pointValidator = new PointValidator();
        ReflectionTestUtils.setField(pointValidator, "minAmount", 1000);
        ReflectionTestUtils.setField(pointValidator, "maxAmount", 1000000);
    }

    @Test
    @DisplayName("정상 범위 내 금액 검증 성공")
    void validatePointAmount_Success() {
        // given
        Integer amount = 5000;

        // when & then
        assertDoesNotThrow(() -> {
            pointValidator.validatePointAmount(amount);
        });
    }

    @Test
    @DisplayName("최소 금액 검증 성공")
    void validatePointAmount_MinAmount_Success() {
        // given
        Integer amount = 1000;

        // when & then
        assertDoesNotThrow(() -> {
            pointValidator.validatePointAmount(amount);
        });
    }

    @Test
    @DisplayName("최대 금액 검증 성공")
    void validatePointAmount_MaxAmount_Success() {
        // given
        Integer amount = 1000000;

        // when & then
        assertDoesNotThrow(() -> {
            pointValidator.validatePointAmount(amount);
        });
    }

    @Test
    @DisplayName("null 금액 검증 시 예외 발생")
    void validatePointAmount_Null_ThrowsException() {
        // given
        Integer amount = null;

        // when & then
        PointException exception = assertThrows(PointException.class, () -> {
            pointValidator.validatePointAmount(amount);
        });
        assertEquals(ErrorCode.POINT_INVALID_AMOUNT, exception.getErrorCode());
    }

    @Test
    @DisplayName("최소 금액 미만 검증 시 예외 발생")
    void validatePointAmount_BelowMin_ThrowsException() {
        // given
        Integer amount = 999;

        // when & then
        PointException exception = assertThrows(PointException.class, () -> {
            pointValidator.validatePointAmount(amount);
        });
        assertEquals(ErrorCode.POINT_INVALID_AMOUNT, exception.getErrorCode());
    }

    @Test
    @DisplayName("0원 검증 시 예외 발생")
    void validatePointAmount_Zero_ThrowsException() {
        // given
        Integer amount = 0;

        // when & then
        PointException exception = assertThrows(PointException.class, () -> {
            pointValidator.validatePointAmount(amount);
        });
        assertEquals(ErrorCode.POINT_INVALID_AMOUNT, exception.getErrorCode());
    }

    @Test
    @DisplayName("음수 금액 검증 시 예외 발생")
    void validatePointAmount_Negative_ThrowsException() {
        // given
        Integer amount = -1000;

        // when & then
        PointException exception = assertThrows(PointException.class, () -> {
            pointValidator.validatePointAmount(amount);
        });
        assertEquals(ErrorCode.POINT_INVALID_AMOUNT, exception.getErrorCode());
    }

    @Test
    @DisplayName("최대 금액 초과 검증 시 예외 발생")
    void validatePointAmount_AboveMax_ThrowsException() {
        // given
        Integer amount = 1000001;

        // when & then
        PointException exception = assertThrows(PointException.class, () -> {
            pointValidator.validatePointAmount(amount);
        });
        assertEquals(ErrorCode.POINT_INVALID_AMOUNT, exception.getErrorCode());
    }

    @Test
    @DisplayName("설정값 변경 시 동적으로 적용")
    void validatePointAmount_DynamicConfiguration() {
        // given
        ReflectionTestUtils.setField(pointValidator, "minAmount", 5000);
        ReflectionTestUtils.setField(pointValidator, "maxAmount", 500000);
        Integer validAmount = 10000;
        Integer invalidAmount = 3000;

        // when & then
        assertDoesNotThrow(() -> {
            pointValidator.validatePointAmount(validAmount);
        });

        assertThrows(PointException.class, () -> {
            pointValidator.validatePointAmount(invalidAmount);
        });
    }
}