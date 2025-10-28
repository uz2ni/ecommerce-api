package com.example.ecommerceapi.controller;

import com.example.ecommerceapi.dto.coupon.CouponResponse;
import com.example.ecommerceapi.dto.coupon.CouponUsageResponse;
import com.example.ecommerceapi.dto.coupon.IssueCouponRequest;
import com.example.ecommerceapi.service.CouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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

        List<CouponResponse> coupons = couponService.getAllCoupons();
        return ResponseEntity.ok(coupons);
    }

    @Operation(summary = "쿠폰 발급", description = "선착순으로 쿠폰을 발급받습니다. 발급 수량이 소진되면 실패합니다.")
    @PostMapping("/issue")
    public ResponseEntity<String> issueCoupon(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "쿠폰 발급 요청")
            @RequestBody IssueCouponRequest request) {

        boolean issued = couponService.issueCoupon(request.getCouponId(), request.getUserId());
        if (!issued) {
            return ResponseEntity.badRequest().body("쿠폰 발급에 실패했습니다. 수량이 부족하거나 존재하지 않는 쿠폰입니다.");
        }

        return ResponseEntity.ok("쿠폰이 발급되었습니다.");
    }

    @Operation(summary = "쿠폰 사용 이력 조회", description = "특정 쿠폰의 사용 이력을 조회합니다.")
    @GetMapping("/{couponId}/usage")
    public ResponseEntity<List<CouponUsageResponse>> getCouponUsageHistory(
            @Parameter(description = "쿠폰 ID", required = true)
            @PathVariable Integer couponId) {

        List<CouponUsageResponse> usageHistory = couponService.getCouponUsageHistory(couponId);
        return ResponseEntity.ok(usageHistory);
    }
}