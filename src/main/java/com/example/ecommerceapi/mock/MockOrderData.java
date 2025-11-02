package com.example.ecommerceapi.mock;

import com.example.ecommerceapi.dto.order.OrderItemResponse;
import com.example.ecommerceapi.dto.order.OrderResponse;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class MockOrderData {

    private static final Map<Integer, OrderResponse> ORDERS = new HashMap<>();
    private static final AtomicInteger ORDER_ID_GENERATOR = new AtomicInteger(1);
    private static final AtomicInteger ORDER_ITEM_ID_GENERATOR = new AtomicInteger(1);

    public static OrderResponse createOrder(Integer userId, String deliveryUsername, String deliveryAddress, Integer couponId) {
        // 장바구니에서 상품 가져오기
        var cartItems = MockCartData.getCartItems(userId);
        if (cartItems.isEmpty()) {
            return null;
        }

        // 주문 상품 생성
        List<OrderItemResponse> orderItems = cartItems.stream()
                .map(cartItem -> {
                    var product = MockProductData.getProduct(cartItem.getProductId());
                    return OrderItemResponse.builder()
                            .orderItemId(ORDER_ITEM_ID_GENERATOR.getAndIncrement())
                            .productId(cartItem.getProductId())
                            .productName(cartItem.getProductName())
                            .description(product != null ? product.getDescription() : "")
                            .productPrice(cartItem.getProductPrice())
                            .orderQuantity(cartItem.getQuantity())
                            .totalPrice(cartItem.getTotalPrice())
                            .build();
                })
                .collect(Collectors.toList());

        // 총 금액 계산
        int totalOrderAmount = orderItems.stream()
                .mapToInt(OrderItemResponse::getTotalPrice)
                .sum();

        // 쿠폰 할인 적용
        int totalDiscountAmount = 0;
        if (couponId != null) {
            var coupon = MockCouponData.getCoupon(couponId);
            if (coupon != null) {
                // 쿠폰 만료일 검증
                if (coupon.getExpiredAt() == null || !LocalDateTime.now().isAfter(coupon.getExpiredAt())) {
                    totalDiscountAmount = coupon.getDiscountAmount();
                }
                // 만료된 쿠폰은 할인 적용하지 않음
            }
        }

        int finalPaymentAmount = totalOrderAmount - totalDiscountAmount;

        OrderResponse order = OrderResponse.builder()
                .orderId(ORDER_ID_GENERATOR.getAndIncrement())
                .userId(userId)
                .orderStatus("PENDING")
                .totalOrderAmount(totalOrderAmount)
                .totalDiscountAmount(totalDiscountAmount)
                .usedPoint(0)
                .finalPaymentAmount(finalPaymentAmount)
                .deliveryUsername(deliveryUsername)
                .deliveryAddress(deliveryAddress)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .orderItems(orderItems)
                .build();

        ORDERS.put(order.getOrderId(), order);
        return order;
    }

    public static OrderResponse getOrder(Integer orderId) {
        return ORDERS.get(orderId);
    }

    public static Map<String, Object> processPayment(Integer orderId, Integer userId) {
        Map<String, Object> result = new HashMap<>();

        OrderResponse order = ORDERS.get(orderId);
        if (order == null) {
            result.put("success", false);
            result.put("message", "주문을 찾을 수 없습니다.");
            return result;
        }

        // 실제 포인트 잔액 조회
        int currentPoint = MockPointData.getBalance(userId);
        int paymentAmount = order.getFinalPaymentAmount();

        if (currentPoint < paymentAmount) {
            result.put("success", false);
            result.put("message", "포인트가 부족합니다.");
            return result;
        }

        // 포인트 차감
        var pointUsage = MockPointData.usePoint(userId, paymentAmount);
        if (pointUsage == null) {
            result.put("success", false);
            result.put("message", "포인트 차감에 실패했습니다.");
            return result;
        }

        // 주문 상태를 결제 완료로 변경
        order.setOrderStatus("PAID");
        order.setUpdatedAt(LocalDateTime.now());

        // 결제 완료 후 장바구니에서 주문 상품 제거
        MockCartData.clearCart(userId);

        result.put("success", true);
        result.put("orderId", orderId);
        result.put("paymentAmount", paymentAmount);
        result.put("remainingPoint", MockUserData.getBalance(userId));
        result.put("message", "결제가 완료되었습니다.");

        return result;
    }
}