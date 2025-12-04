package com.example.ecommerceapi.coupon.presentation.controller;

import com.example.ecommerceapi.coupon.application.dto.CouponResult;
import com.example.ecommerceapi.coupon.application.dto.CouponUserResult;
import com.example.ecommerceapi.coupon.application.dto.IssueCouponResult;
import com.example.ecommerceapi.coupon.application.service.CouponService;
import com.example.ecommerceapi.coupon.presentation.dto.CouponResponse;
import com.example.ecommerceapi.coupon.presentation.dto.CouponUserResponse;
import com.example.ecommerceapi.coupon.presentation.dto.IssueCouponRequest;
import com.example.ecommerceapi.coupon.presentation.dto.IssueCouponResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "쿠폰", description = "쿠폰 관리 API")
@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    @Operation(summary = "쿠폰 정보 목록 조회", description = "발급 가능한 쿠폰 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<List<CouponResponse>> getCoupons() {
        List<CouponResult> coupons = couponService.getAllCoupons();
        return ResponseEntity.ok(CouponResponse.fromList(coupons));
    }

    @Operation(summary = "쿠폰 발급 (동기)", description = "쿠폰을 즉시 발급받습니다. 발급 수량이 소진되거나 중복 발급 시 실패합니다.")
    @PostMapping("/issue")
    public ResponseEntity<IssueCouponResponse> issueCoupon(
            @Valid @RequestBody IssueCouponRequest request) {

        IssueCouponResult result = couponService.issueCoupon(request.toCommand());
        return ResponseEntity.ok(IssueCouponResponse.from(result));
    }

    @Operation(summary = "쿠폰 발급 접수 (비동기)", description = "쿠폰 발급 요청을 접수합니다. 실제 발급은 비동기로 처리됩니다.")
    @PostMapping("/issue/request")
    public ResponseEntity<IssueCouponResponse> issueCouponAsync(
            @Valid @RequestBody IssueCouponRequest request) {

        IssueCouponResult result = couponService.issueCouponAsync(request.toCommand());
        return ResponseEntity.ok(IssueCouponResponse.from(result));
    }

    @Operation(summary = "쿠폰 사용 이력 조회", description = "특정 쿠폰의 발급 이력을 조회합니다.")
    @GetMapping("/{couponId}/usage")
    public ResponseEntity<List<CouponUserResponse>> getCouponUsageHistory(
            @Parameter(description = "쿠폰 ID", required = true)
            @PathVariable Integer couponId) {

        List<CouponUserResult> usageHistory = couponService.getCouponUsageHistory(couponId);
        return ResponseEntity.ok(CouponUserResponse.fromList(usageHistory));
    }
}