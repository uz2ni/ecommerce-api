package com.example.ecommerceapi.coupon.infrastructure.persistence;

import com.example.ecommerceapi.coupon.domain.entity.Coupon;
import com.example.ecommerceapi.coupon.domain.entity.CouponUser;
import com.example.ecommerceapi.coupon.domain.repository.CouponUserRepository;
import com.example.ecommerceapi.user.domain.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * CouponUserRepository의 JPA 구현체
 * JpaCouponUserRepository를 사용하여 실제 DB 연동
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class JpaCouponUserRepositoryAdapter implements CouponUserRepository {

    private final JpaCouponUserRepository jpaCouponUserRepository;
    private final CouponUserTableUtils couponUserTableUtils;

    @Override
    public CouponUser save(CouponUser couponUser) {
        return jpaCouponUserRepository.save(couponUser);
    }

    @Override
    public Optional<CouponUser> findById(Integer couponUserId) {
        return jpaCouponUserRepository.findById(couponUserId);
    }

    @Override
    public List<CouponUser> findByCouponId(Integer couponId) {
        return jpaCouponUserRepository.findByCoupon_CouponId(couponId);
    }

    @Override
    public Optional<CouponUser> findByCouponIdAndUserId(Integer couponId, Integer userId) {
        return jpaCouponUserRepository.findByCoupon_CouponIdAndUser_UserId(couponId, userId);
    }

    @Override
    public Optional<CouponUser> findByCouponIdAndUserIdWithPessimisticLock(Integer couponId, Integer userId) {
        return jpaCouponUserRepository.findByCouponIdAndUserIdWithPessimisticLock(couponId, userId);
    }

    @Override
    public Optional<CouponUser> findByCouponIdAndUserIdWithOptimisticLock(Integer couponId, Integer userId) {
        return jpaCouponUserRepository.findByCouponIdAndUserIdWithOptimisticLock(couponId, userId);
    }

    @Override
    public List<CouponUser> findByUserId(Integer userId) {
        return jpaCouponUserRepository.findByUser_UserId(userId);
    }

    @Override
    public void clear() {
        couponUserTableUtils.resetCouponUserTable();
    }

    @Override
    public int count() {
        return (int) jpaCouponUserRepository.count();
    }

    @Override
    public void init() {
        // 1. 테이블 초기화 (AUTO_INCREMENT 리셋)
        couponUserTableUtils.resetCouponUserTable();

        // 2. 쿠폰 객체 생성
        Coupon coupon1 = Coupon.builder().couponId(1).version(1).build();
        Coupon coupon2 = Coupon.builder().couponId(2).version(1).build();
        Coupon coupon3 = Coupon.builder().couponId(3).version(1).build();

        // 3. 초기 쿠폰 발급 이력 생성
        // 쿠폰 1 발급 이력
        save(CouponUser.builder()
                .coupon(coupon1)
                .user(User.builder().userId(1).build())
                .used(true)
                .issuedAt(LocalDateTime.now().minusDays(5))
                .usedAt(LocalDateTime.now().minusDays(3))
                .version(1)
                .build());

        save(CouponUser.builder()
                .coupon(coupon1)
                .user(User.builder().userId(2).build())
                .used(false)
                .issuedAt(LocalDateTime.now().minusDays(4))
                .usedAt(null)
                .version(1)
                .build());

        save(CouponUser.builder()
                .coupon(coupon1)
                .user(User.builder().userId(3).build())
                .used(false)
                .issuedAt(LocalDateTime.now().minusDays(2))
                .usedAt(null)
                .version(1)
                .build());

        // 쿠폰 2 발급 이력
        save(CouponUser.builder()
                .coupon(coupon2)
                .user(User.builder().userId(1).build())
                .used(false)
                .issuedAt(LocalDateTime.now().minusDays(10))
                .usedAt(null)
                .version(1)
                .build());

        save(CouponUser.builder()
                .coupon(coupon2)
                .user(User.builder().userId(2).build())
                .used(false)
                .issuedAt(LocalDateTime.now().minusDays(9))
                .usedAt(null)
                .version(1)
                .build());

        save(CouponUser.builder()
                .coupon(coupon2)
                .user(User.builder().userId(3).build())
                .used(false)
                .issuedAt(LocalDateTime.now().minusDays(8))
                .usedAt(null)
                .version(1)
                .build());

        // 쿠폰 3 발급 이력
        save(CouponUser.builder()
                .coupon(coupon3)
                .user(User.builder().userId(1).build())
                .used(false)
                .issuedAt(LocalDateTime.now().minusDays(3))
                .usedAt(null)
                .version(1)
                .build());

        save(CouponUser.builder()
                .coupon(coupon3)
                .user(User.builder().userId(4).build())
                .used(false)
                .issuedAt(LocalDateTime.now().minusDays(2))
                .usedAt(null)
                .version(1)
                .build());

        log.info("초기 쿠폰 발급 이력 데이터 생성 완료");
    }
}
