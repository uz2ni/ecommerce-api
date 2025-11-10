package com.example.ecommerceapi.order.presentation.controller;

import com.example.ecommerceapi.order.application.dto.CreateOrderResult;
import com.example.ecommerceapi.order.application.dto.OrderResult;
import com.example.ecommerceapi.order.application.dto.PaymentResult;
import com.example.ecommerceapi.order.application.service.OrderService;
import com.example.ecommerceapi.order.presentation.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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

    private final OrderService orderService;

    @Operation(
            summary = "주문 생성",
            description = "장바구니에 담긴 상품으로 주문을 생성합니다. 장바구니가 비어있거나 재고가 부족하면 실패합니다."
    )
    @PostMapping
    public ResponseEntity<CreateOrderResponse> createOrder(
            @Valid @RequestBody CreateOrderRequest request) {

        CreateOrderResult order = orderService.createOrder(CreateOrderRequest.toCreateOrderCommand(request));
        return ResponseEntity.ok(CreateOrderResponse.from(order));
    }

    @Operation(
            summary = "주문 내역 조회",
            description = "주문 ID로 주문 내역을 조회합니다."
    )
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(
            @Parameter(description = "주문 ID", required = true)
            @PathVariable Integer orderId) {

        OrderResult order = orderService.getOrder(orderId);
        return ResponseEntity.ok(OrderResponse.buildGetOrder(order));
    }

    @Operation(
            summary = "결제",
            description = "포인트로 주문 금액을 결제합니다. 포인트가 부족하면 실패하고, 성공 시 장바구니에서 주문 상품이 제거됩니다."
    )
    @PostMapping("/payment")
    public ResponseEntity<PaymentResponse> payment(
            @Valid @RequestBody PaymentRequest request) {

        PaymentResult payment = orderService.processPayment(request.orderId(), request.userId());
        return ResponseEntity.ok(PaymentResponse.from(payment));
    }
}