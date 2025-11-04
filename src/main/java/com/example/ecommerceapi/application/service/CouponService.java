package com.example.ecommerceapi.application.service;

import com.example.ecommerceapi.application.dto.coupon.CouponResponse;
import com.example.ecommerceapi.application.dto.coupon.CouponUsageResponse;
import com.example.ecommerceapi.application.usecase.CouponUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Primary
@RequiredArgsConstructor
public class CouponService implements CouponUseCase {

    @Override
    public List<CouponResponse> getAllCoupons() {
        // TODO: Implement
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public CouponResponse getCoupon(Integer couponId) {
        // TODO: Implement
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public boolean issueCoupon(Integer couponId, Integer userId) {
        // TODO: Implement
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public List<CouponUsageResponse> getCouponUsageHistory(Integer couponId) {
        // TODO: Implement
        throw new UnsupportedOperationException("Not implemented yet");
    }
}