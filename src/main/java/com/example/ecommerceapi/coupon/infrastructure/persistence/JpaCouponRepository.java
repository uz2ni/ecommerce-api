package com.example.ecommerceapi.coupon.infrastructure.persistence;

import com.example.ecommerceapi.coupon.domain.entity.Coupon;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * Coupon 도메인의 JPA Repository
 * 선착순 쿠폰 발급을 위해 비관적 락 지원
 */
public interface JpaCouponRepository extends JpaRepository<Coupon, Integer> {

    /**
     * 비관적 락을 사용하여 쿠폰 조회
     * 선착순 쿠폰 발급 시 동시성 제어를 위해 사용
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Coupon c WHERE c.couponId = :couponId")
    Optional<Coupon> findByIdWithPessimisticLock(@Param("couponId") Integer couponId);

}
