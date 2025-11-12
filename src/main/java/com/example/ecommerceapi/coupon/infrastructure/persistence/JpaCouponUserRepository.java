package com.example.ecommerceapi.coupon.infrastructure.persistence;

import com.example.ecommerceapi.coupon.domain.entity.CouponUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * CouponUser 도메인의 JPA Repository
 */
public interface JpaCouponUserRepository extends JpaRepository<CouponUser, Integer> {

    /**
     * 쿠폰 ID로 발급 이력 목록 조회
     */
    List<CouponUser> findByCoupon_CouponId(Integer couponId);

    /**
     * 쿠폰 ID와 사용자 ID로 발급 이력 조회
     */
    Optional<CouponUser> findByCoupon_CouponIdAndUser_UserId(Integer couponId, Integer userId);

    /**
     * 사용자 ID로 발급 이력 목록 조회
     */
    List<CouponUser> findByUser_UserId(Integer userId);
}
