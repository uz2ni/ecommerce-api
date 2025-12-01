package com.example.ecommerceapi.user.domain.entity;

import com.example.ecommerceapi.common.exception.ErrorCode;
import com.example.ecommerceapi.common.exception.PointException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer userId;

    @Column(nullable = false, length = 100)
    private String username;

    @Column(name = "point_balance", nullable = false)
    private Integer pointBalance;

    // @Version
    @Column(name = "version")
    private Integer version;

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