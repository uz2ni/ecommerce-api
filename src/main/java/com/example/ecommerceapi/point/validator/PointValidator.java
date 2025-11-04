package com.example.ecommerceapi.point.validator;

import com.example.ecommerceapi.common.exception.ErrorCode;
import com.example.ecommerceapi.point.exception.PointException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 포인트 금액 검증 담당 컴포넌트
 */
@Component
public class PointValidator {

    @Value("${constant.point.min-amount}")
    private Integer minAmount;

    @Value("${constant.point.max-amount}")
    private Integer maxAmount;

    /**
     * 포인트 금액이 유효한지 검증합니다.
     * @param amount 검증할 금액
     * @throws PointException 금액이 null이거나 범위를 벗어난 경우
     */
    public void validatePointAmount(Integer amount) {
        if (amount == null) {
            throw new PointException(ErrorCode.POINT_INVALID_AMOUNT);
        }

        if (amount < minAmount) {
            throw new PointException(ErrorCode.POINT_INVALID_AMOUNT);
        }

        if (amount > maxAmount) {
            throw new PointException(ErrorCode.POINT_INVALID_AMOUNT);
        }
    }
}