package com.example.ecommerceapi.coupon.application.validator;

import com.example.ecommerceapi.common.exception.CouponException;
import com.example.ecommerceapi.common.exception.ErrorCode;
import com.example.ecommerceapi.coupon.domain.entity.Coupon;
import com.example.ecommerceapi.coupon.infrastructure.InMemoryCouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CouponValidator {

    private final InMemoryCouponRepository couponRepository;

    /**
     * 쿠폰 존재 여부를 검증하고 Coupon 엔티티를 반환합니다.
     * @param couponId 검증할 coupon ID
     * @return Coupon 엔티티
     * @throws CouponException 쿠폰이 존재하지 않을 경우
     */
    public Coupon validateAndGetCoupon(Integer couponId) {
        Optional<Coupon> coupon = couponRepository.findById(couponId);
        if (coupon.isEmpty()) {
            throw new CouponException(ErrorCode.COUPON_NOT_FOUND);
        }
        return coupon.get();
    }
}