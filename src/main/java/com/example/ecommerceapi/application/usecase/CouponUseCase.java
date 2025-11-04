package com.example.ecommerceapi.application.usecase;

import com.example.ecommerceapi.application.dto.coupon.CouponResponse;
import com.example.ecommerceapi.application.dto.coupon.CouponUsageResponse;

import java.util.List;

public interface CouponUseCase {

    List<CouponResponse> getAllCoupons();

    CouponResponse getCoupon(Integer couponId);

    boolean issueCoupon(Integer couponId, Integer userId);

    List<CouponUsageResponse> getCouponUsageHistory(Integer couponId);
}