package com.example.ecommerceapi.user.domain.entity;

import com.example.ecommerceapi.common.aspect.WithLock;
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
     */
    public void chargePoints(Integer amount) {
        this.pointBalance += amount;
    }

    /**
     * 포인트를 사용합니다.
     * @param amount 사용할 금액
     * @throws PointException 잔액이 부족할 경우
     */
    public void usePoints(Integer amount) {
        if (this.pointBalance < amount) {
            throw new PointException(ErrorCode.POINT_INSUFFICIENT_BALANCE);
        }
        this.pointBalance -= amount;
    }

    /**
     * 포인트를 환불합니다.
     * @param amount 환불할 금액
     */
    public void refundPoints(Integer amount) {
        this.pointBalance += amount;
    }

    /**
     * 포인트를 원상복구합니다. (보상 트랜잭션, 결제 취소)
     * @param amount 충전할 금액
     */
    public void addPoints(Integer amount) {
        this.pointBalance += amount;
    }
}