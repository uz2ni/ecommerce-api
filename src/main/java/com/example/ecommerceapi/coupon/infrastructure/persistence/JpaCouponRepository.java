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
}
