package com.example.ecommerceapi.coupon.usecase;

import com.example.ecommerceapi.coupon.dto.CouponResponse;
import com.example.ecommerceapi.coupon.dto.CouponUsageResponse;

import java.util.List;

public interface CouponUseCase {

    List<CouponResponse> getAllCoupons();

    CouponResponse getCoupon(Integer couponId);

    boolean issueCoupon(Integer couponId, Integer userId);

    List<CouponUsageResponse> getCouponUsageHistory(Integer couponId);
}