package com.example.ecommerceapi.order.domain.entity;

import com.example.ecommerceapi.common.exception.ErrorCode;
import com.example.ecommerceapi.common.exception.OrderException;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    private Integer orderId;
    private Integer userId;
    private OrderStatus orderStatus;
    private Integer totalOrderAmount;
    private Integer totalDiscountAmount;
    private Integer usedPoint;
    private Integer finalPaymentAmount;
    private String deliveryUsername;
    private String deliveryAddress;
    private Integer couponId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * 주문을 생성합니다.
     */
    public static Order createOrder(Integer userId,
                                     String deliveryUsername,
                                     String deliveryAddress,
                                     Integer totalOrderAmount,
                                     Integer discountAmount,
                                     Integer couponId) {
        Integer finalAmount = totalOrderAmount - discountAmount;
        return Order.builder()
                .userId(userId)
                .orderStatus(OrderStatus.PENDING)
                .totalOrderAmount(totalOrderAmount)
                .totalDiscountAmount(discountAmount)
                .usedPoint(0)
                .finalPaymentAmount(finalAmount)
                .deliveryUsername(deliveryUsername)
                .deliveryAddress(deliveryAddress)
                .couponId(couponId)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * 주문을 결제 완료 상태로 변경합니다.
     */
    public void completePayment() {
        if (this.orderStatus == OrderStatus.PAID) {
            throw new OrderException(ErrorCode.ORDER_ALREADY_PAID);
        }
        this.orderStatus = OrderStatus.PAID;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 주문을 취소합니다.
     */
    public void cancel() {
        this.orderStatus = OrderStatus.CANCELLED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 결제 가능 상태를 검증합니다.
     */
    public void validatePaymentAvailable() {
        if (this.orderStatus != OrderStatus.PENDING) {
            throw new OrderException(ErrorCode.ORDER_PAY_INVALID_STATUS);
        }
    }

    /**
     * 해당 주문에 쿠폰 적용 여부를 확인합니다.
     */
    public boolean hasCoupon() {
        return this.couponId != null;
    }

    /**
     * 결제 실패 시 주문 상태를 실패로 변경합니다.
     */
    public void markPaymentFailed() {
        this.orderStatus = OrderStatus.FAILED;
    }
}