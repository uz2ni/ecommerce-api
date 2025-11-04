package com.example.ecommerceapi.presentation.controller;

import com.example.ecommerceapi.application.dto.order.CreateOrderRequest;
import com.example.ecommerceapi.application.dto.order.OrderResponse;
import com.example.ecommerceapi.application.dto.order.PaymentRequest;
import com.example.ecommerceapi.application.dto.order.PaymentResponse;
import com.example.ecommerceapi.application.usecase.OrderUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "주문/결제", description = "주문 및 결제 관리 API")
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderUseCase orderUseCase;

    @Operation(
            summary = "주문 생성",
            description = "장바구니에 담긴 상품으로 주문을 생성합니다. 장바구니가 비어있거나 재고가 부족하면 실패합니다."
    )
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody CreateOrderRequest request) {

        OrderResponse response = orderUseCase.createOrder(
                request.getUserId(),
                request.getDeliveryUsername(),
                request.getDeliveryAddress(),
                request.getCouponId()
        );

        if (response == null) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "주문 내역 조회",
            description = "주문 ID로 주문 내역을 조회합니다."
    )
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(
            @io.swagger.v3.oas.annotations.Parameter(description = "주문 ID", required = true)
            @PathVariable Integer orderId) {

        OrderResponse order = orderUseCase.getOrder(orderId);
        if (order == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(order);
    }

    @Operation(
            summary = "결제",
            description = "포인트로 주문 금액을 결제합니다. 포인트가 부족하면 실패하고, 성공 시 장바구니에서 주문 상품이 제거됩니다."
    )
    @PostMapping("/payment")
    public ResponseEntity<PaymentResponse> payment(
            @Valid @RequestBody PaymentRequest request) {

        Map<String, Object> result = orderUseCase.processPayment(
                request.getOrderId(),
                request.getUserId()
        );

        boolean success = (boolean) result.get("success");
        if (!success) {
            return ResponseEntity.badRequest()
                    .body(PaymentResponse.builder()
                            .message((String) result.get("message"))
                            .build());
        }

        PaymentResponse response = PaymentResponse.builder()
                .orderId((Integer) result.get("orderId"))
                .paymentAmount((Integer) result.get("paymentAmount"))
                .remainingPoint((Integer) result.get("remainingPoint"))
                .message((String) result.get("message"))
                .build();

        return ResponseEntity.ok(response);
    }
}