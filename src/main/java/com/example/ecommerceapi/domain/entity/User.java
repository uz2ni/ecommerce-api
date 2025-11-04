package com.example.ecommerceapi.domain.entity;

import com.example.ecommerceapi.common.exception.ErrorCode;
import com.example.ecommerceapi.common.exception.PointException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Integer userId;
    private String username;
    private Integer pointBalance;

    /**
     * 포인트를 충전합니다.
     * @param amount 충전할 금액
     * @throws PointException 충전 금액이 유효하지 않을 경우
     */
    public void chargePoints(Integer amount) {
        validatePointAmount(amount);
        this.pointBalance += amount;
    }

    /**
     * 포인트를 사용합니다.
     * @param amount 사용할 금액
     * @throws PointException 사용 금액이 유효하지 않거나 잔액이 부족할 경우
     */
    public void usePoints(Integer amount) {
        validatePointAmount(amount);
        if (this.pointBalance < amount) {
            throw new PointException(ErrorCode.POINT_INSUFFICIENT_BALANCE);
        }
        this.pointBalance -= amount;
    }

    /**
     * 포인트를 환불합니다.
     * @param amount 환불할 금액
     * @throws PointException 환불 금액이 유효하지 않을 경우
     */
    public void refundPoints(Integer amount) {
        validatePointAmount(amount);
        this.pointBalance += amount;
    }

    /**
     * 포인트 금액의 유효성을 검증합니다.
     * @param amount 검증할 금액
     * @throws PointException 금액이 null이거나 0 이하일 경우
     */
    private void validatePointAmount(Integer amount) {
        if (amount == null || amount <= 0) {
            throw new PointException(ErrorCode.POINT_INVALID_AMOUNT);
        }
    }

}