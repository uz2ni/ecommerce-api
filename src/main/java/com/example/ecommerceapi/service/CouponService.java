package com.example.ecommerceapi.service;

import com.example.ecommerceapi.dto.coupon.CouponResponse;
import com.example.ecommerceapi.dto.coupon.CouponUsageResponse;
import com.example.ecommerceapi.mock.MockCouponData;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CouponService {

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