package com.example.ecommerceapi.coupon.domain.repository;

import com.example.ecommerceapi.coupon.domain.entity.CouponUser;

import java.util.List;
import java.util.Optional;

/**
 * CouponUser 도메인의 Repository 인터페이스
 * 구현체: InMemoryCouponUserRepository (추후 JpaCouponUserRepository 등으로 확장 가능)
 */
public interface CouponUserRepository {

    /**
     * 쿠폰 발급 이력 저장
     */
    CouponUser save(CouponUser couponUser);

    /**
     * ID로 쿠폰 발급 이력 조회
     */
    Optional<CouponUser> findById(Integer couponUserId);

    /**
     * 쿠폰 ID로 발급 이력 목록 조회
     */
    List<CouponUser> findByCouponId(Integer couponId);

    /**
     * 쿠폰 ID와 사용자 ID로 발급 이력 조회
     */
    Optional<CouponUser> findByCouponIdAndUserId(Integer couponId, Integer userId);

    /**
     * 비관적 락을 사용하여 쿠폰 ID와 사용자 ID로 발급 이력 조회
     * 중복 발급 검증 시 동시성 제어를 위해 사용
     */
    Optional<CouponUser> findByCouponIdAndUserIdWithPessimisticLock(Integer couponId, Integer userId);

    /**
     * 낙관적 락을 사용하여 쿠폰 ID와 사용자 ID로 발급 이력 조회
     * 결제 시 쿠폰 사용 처리 등 동시성 제어를 위해 사용
     */
    Optional<CouponUser> findByCouponIdAndUserIdWithOptimisticLock(Integer couponId, Integer userId);

    /**
     * 사용자 ID로 발급 이력 목록 조회
     */
    List<CouponUser> findByUserId(Integer userId);

    /**
     * 모든 쿠폰 발급 이력 삭제 (테스트용)
     */
    void clear();

    /**
     * 쿠폰 발급 이력 개수 조회
     */
    int count();

    /**
     * 초기 쿠폰 발급 이력 데이터 생성 (테스트용)
     */
    void init();
}