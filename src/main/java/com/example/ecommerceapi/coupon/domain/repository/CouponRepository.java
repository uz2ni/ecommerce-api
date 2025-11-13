package com.example.ecommerceapi.coupon.domain.repository;
import com.example.ecommerceapi.coupon.domain.entity.Coupon;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

/**
 * Coupon 도메인의 Repository 인터페이스
 * 구현체: InMemoryCouponRepository (추후 JpaCouponRepository 등으로 확장 가능)
 */
public interface CouponRepository {

    /**
     * 쿠폰 저장
     */
    Coupon save(Coupon coupon);

    /**
     * ID로 쿠폰 조회
     */
    Optional<Coupon> findById(Integer couponId);

    /**
     * 비관적 락을 사용하여 쿠폰 조회
     * 선착순 쿠폰 발급 시 동시성 제어를 위해 사용
     */
    Optional<Coupon> findByIdWithPessimisticLock(Integer couponId);

    /**
     * 모든 쿠폰 조회
     */
    List<Coupon> findAll();

    /**
     * 모든 쿠폰 삭제 (테스트용)
     */
    void clear();

    /**
     * 쿠폰 개수 조회
     */
    int count();

    /**
     * 초기 쿠폰 데이터 생성 (테스트용)
     */
    void init();
}