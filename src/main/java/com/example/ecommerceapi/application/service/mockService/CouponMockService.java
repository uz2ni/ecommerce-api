package com.example.ecommerceapi.application.service.mockService;

import com.example.ecommerceapi.application.dto.coupon.CouponResponse;
import com.example.ecommerceapi.application.dto.coupon.CouponUsageResponse;
import com.example.ecommerceapi.application.usecase.CouponUseCase;
import com.example.ecommerceapi.infrastructure.memory.MockCouponData;
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