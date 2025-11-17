package com.example.ecommerceapi.coupon.infrastructure.persistence;

import com.example.ecommerceapi.coupon.domain.entity.CouponUser;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
     * 비관적 락을 사용하여 쿠폰 ID와 사용자 ID로 발급 이력 조회
     * 중복 발급 검증 시 동시성 제어를 위해 사용
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT cu FROM CouponUser cu WHERE cu.coupon.couponId = :couponId AND cu.user.userId = :userId")
    Optional<CouponUser> findByCouponIdAndUserIdWithPessimisticLock(@Param("couponId") Integer couponId, @Param("userId") Integer userId);

    /**
     * 사용자 ID로 발급 이력 목록 조회
     */
    List<CouponUser> findByUser_UserId(Integer userId);
}
