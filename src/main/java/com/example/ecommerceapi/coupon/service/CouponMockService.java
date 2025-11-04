package com.example.ecommerceapi.coupon.service;

import com.example.ecommerceapi.coupon.dto.CouponResponse;
import com.example.ecommerceapi.coupon.dto.CouponUsageResponse;
import com.example.ecommerceapi.coupon.usecase.CouponUseCase;
import com.example.ecommerceapi.coupon.infrastructure.MockCouponData;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CouponMockService implements CouponUseCase {

    public List<CouponResponse> getAllCoupons() {
        return MockCouponData.getAllCoupons();
    }

    public CouponResponse getCoupon(Integer couponId) {
        return MockCouponData.getCoupon(couponId);
    }

    public boolean issueCoupon(Integer couponId, Integer userId) {
        return MockCouponData.issueCoupon(couponId, userId);
    }

    public List<CouponUsageResponse> getCouponUsageHistory(Integer couponId) {
        return MockCouponData.getCouponUsageHistory(couponId);
    }
}