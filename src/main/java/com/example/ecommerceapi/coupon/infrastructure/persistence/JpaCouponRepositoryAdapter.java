package com.example.ecommerceapi.coupon.infrastructure.persistence;

import com.example.ecommerceapi.coupon.domain.entity.Coupon;
import com.example.ecommerceapi.coupon.domain.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * CouponRepository의 JPA 구현체
 * JpaCouponRepository를 사용하여 실제 DB 연동
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class JpaCouponRepositoryAdapter implements CouponRepository {

    private final JpaCouponRepository jpaCouponRepository;
    private final CouponTableUtils couponTableUtils;

    @Override
    public Coupon save(Coupon coupon) {
        return jpaCouponRepository.save(coupon);
    }

    @Override
    public Optional<Coupon> findById(Integer couponId) {
        return jpaCouponRepository.findById(couponId);
    }

    @Override
    public Optional<Coupon> findByIdWithPessimisticLock(Integer couponId) {
        return jpaCouponRepository.findByIdWithPessimisticLock(couponId);
    }

    @Override
    public List<Coupon> findAll() {
        return jpaCouponRepository.findAll();
    }

    @Override
    public void clear() {
        couponTableUtils.resetCouponTable();
    }

    @Override
    public int count() {
        return (int) jpaCouponRepository.count();
    }

    @Override
    public void init() {
        // 1. 테이블 초기화 (AUTO_INCREMENT 리셋)
        couponTableUtils.resetCouponTable();

        // 2. 초기 쿠폰 데이터 생성
        // 쿠폰 1: 신규 오픈 선착순 할인 쿠폰
        save(Coupon.builder()
                .couponName("신규 오픈 선착순 할인 쿠폰")
                .discountAmount(20000)
                .totalQuantity(50)
                .issuedQuantity(3)
                .expiredAt(LocalDateTime.now().plusDays(30))
                .createdAt(LocalDateTime.now().minusDays(5))
                .build());

        // 쿠폰 2: 3명 한정 선착순 할인 쿠폰 (소진됨)
        save(Coupon.builder()
                .couponName("3명 한정 선착순 할인 쿠폰")
                .discountAmount(15000)
                .totalQuantity(3)
                .issuedQuantity(3)
                .expiredAt(LocalDateTime.now().plusDays(15))
                .createdAt(LocalDateTime.now().minusDays(10))
                .build());

        // 쿠폰 3: 누구나 선착순 할인 쿠폰
        save(Coupon.builder()
                .couponName("누구나 선착순 할인 쿠폰")
                .discountAmount(10000)
                .totalQuantity(30)
                .issuedQuantity(2)
                .expiredAt(LocalDateTime.now().plusDays(60))
                .createdAt(LocalDateTime.now().minusDays(3))
                .build());

        log.info("초기 쿠폰 데이터 생성 완료");
    }
}
