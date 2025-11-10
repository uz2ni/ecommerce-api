package com.example.ecommerceapi.cart.presentation.controller;

import com.example.ecommerceapi.cart.application.dto.CartItemResult;
import com.example.ecommerceapi.cart.presentation.dto.AddCartItemRequest;
import com.example.ecommerceapi.cart.presentation.dto.CartItemResponse;
import com.example.ecommerceapi.cart.application.service.CartService;
import com.example.ecommerceapi.cart.presentation.dto.DeleteCartItemResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "장바구니", description = "장바구니 관리 API")
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @Operation(summary = "장바구니 상품 목록 조회", description = "사용자의 장바구니에 담긴 상품 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<List<CartItemResponse>> getCartItems(
            @Parameter(description = "회원 ID", required = true)
            @RequestParam Integer userId) {

        List<CartItemResult> cartItems = cartService.getCartItems(userId);
        return ResponseEntity.ok(CartItemResponse.fromList(cartItems));
    }

    @Operation(summary = "장바구니 상품 등록", description = "장바구니에 상품을 추가합니다.")
    @PostMapping
    public ResponseEntity<CartItemResponse> addCartItem(
            @Valid @RequestBody AddCartItemRequest request) {

        CartItemResult cartItem = cartService.addCartItem(request.toCommand());
        return ResponseEntity.ok(CartItemResponse.from(cartItem));
    }

    @Operation(summary = "장바구니 상품 삭제", description = "장바구니에서 상품을 삭제합니다.")
    @DeleteMapping("/{cartItemId}")
    public ResponseEntity<DeleteCartItemResponse> deleteCartItem(
            @Parameter(description = "장바구니 상품 ID", required = true)
            @PathVariable Integer cartItemId) {

        Integer deleteCartItemId = cartService.deleteCartItem(cartItemId);

        DeleteCartItemResponse response = new DeleteCartItemResponse(deleteCartItemId);

        return ResponseEntity.ok(response);
    }
}