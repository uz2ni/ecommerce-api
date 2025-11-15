package com.example.ecommerceapi.order.domain.entity;

import com.example.ecommerceapi.common.exception.ErrorCode;
import com.example.ecommerceapi.common.exception.OrderException;
import com.example.ecommerceapi.coupon.domain.entity.Coupon;
import com.example.ecommerceapi.user.domain.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "orders", indexes = {
    @Index(name = "idx_orders_user_id", columnList = "user_id"),
    @Index(name = "idx_orders_status_createdat", columnList = "order_status, created_at")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Integer orderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false, length = 20)
    private OrderStatus orderStatus;

    @Column(name = "total_order_amount", nullable = false)
    private Integer totalOrderAmount;

    @Column(name = "total_discount_amount", nullable = false)
    private Integer totalDiscountAmount;

    @Column(name = "used_point", nullable = false)
    private Integer usedPoint;

    @Column(name = "final_payment_amount", nullable = false)
    private Integer finalPaymentAmount;

    @Column(name = "delivery_username", nullable = false, length = 100)
    private String deliveryUsername;

    @Column(name = "delivery_address", nullable = false, length = 500)
    private String deliveryAddress;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Coupon coupon;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 주문을 생성합니다.
     */
    public static Order createOrder(User user,
                                     String deliveryUsername,
                                     String deliveryAddress,
                                     Integer totalOrderAmount,
                                     Integer discountAmount,
                                     Coupon coupon) {
        Integer finalAmount = totalOrderAmount - discountAmount;
        return Order.builder()
                .user(user)
                .orderStatus(OrderStatus.PENDING)
                .totalOrderAmount(totalOrderAmount)
                .totalDiscountAmount(discountAmount)
                .usedPoint(0)
                .finalPaymentAmount(finalAmount)
                .deliveryUsername(deliveryUsername)
                .deliveryAddress(deliveryAddress)
                .coupon(coupon)
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
        return this.coupon != null;
    }

    /**
     * 결제 실패 시 주문 상태를 실패로 변경합니다.
     */
    public void markPaymentFailed() {
        this.orderStatus = OrderStatus.FAILED;
    }
}