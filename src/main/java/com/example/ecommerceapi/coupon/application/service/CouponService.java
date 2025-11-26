package com.example.ecommerceapi.coupon.application.service;

import com.example.ecommerceapi.common.exception.CouponException;
import com.example.ecommerceapi.common.exception.ErrorCode;
import com.example.ecommerceapi.common.lock.DistributedLock;
import com.example.ecommerceapi.common.lock.LockType;
import com.example.ecommerceapi.coupon.application.dto.CouponResult;
import com.example.ecommerceapi.coupon.application.dto.CouponUserResult;
import com.example.ecommerceapi.coupon.application.dto.IssueCouponCommand;
import com.example.ecommerceapi.coupon.application.dto.IssueCouponResult;
import com.example.ecommerceapi.coupon.application.validator.CouponValidator;
import com.example.ecommerceapi.coupon.domain.entity.Coupon;
import com.example.ecommerceapi.coupon.domain.entity.CouponUser;
import com.example.ecommerceapi.coupon.domain.repository.CouponRepository;
import com.example.ecommerceapi.coupon.domain.repository.CouponUserRepository;
import com.example.ecommerceapi.user.application.validator.UserValidator;
import com.example.ecommerceapi.user.domain.entity.User;
import com.example.ecommerceapi.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponValidator couponValidator;
    private final UserValidator userValidator;
    private final CouponRepository couponRepository;
    private final CouponUserRepository couponUserRepository;
    private final UserRepository userRepository;

    /**
     * 쿠폰 정보 목록 조회
     */
    @Transactional(readOnly = true)
    public List<CouponResult> getAllCoupons() {
        List<Coupon> coupons = couponRepository.findAll();
        return CouponResult.fromList(coupons);
    }

    /**
     * 쿠폰 발급
     * - 선착순으로 쿠폰을 발급
     * - 중복 발급 불가
     * - 발급 수량이 소진되면 실패
     * - 쿠폰이 만료되면 실패
     * <분산 락-PUB_SUB>
     * coupon:#command.couponId  // 쿠폰 발급 동시성 제어
     */
    @DistributedLock(key = "'coupon' + #command.couponId", type = LockType.PUB_SUB, waitTime = 5, leaseTime = 10)
    @Transactional
    public IssueCouponResult issueCoupon(IssueCouponCommand command) {
        // 1. 회원 존재 검증
        User user = userValidator.validateAndGetUser(command.userId());

        // 2. 쿠폰 존재 검증
        Coupon coupon = couponRepository.findById(command.couponId())
                .orElseThrow(() -> new CouponException(ErrorCode.COUPON_NOT_FOUND));

        // 3. 중복 발급 검증
        Optional<CouponUser> existingCouponUser = couponUserRepository
                .findByCouponIdAndUserId(command.couponId(), command.userId());
        if (existingCouponUser.isPresent()) {
            throw new CouponException(ErrorCode.COUPON_ALREADY_ISSUED);
        }

        // 4. 쿠폰 발급 가능 여부 검증 (수량, 만료일)
        // 도메인 엔티티의 issueCoupon() 메서드가 검증을 수행하고 발급 수량을 증가시킴
        coupon.issueCoupon();

        // 5. 쿠폰 업데이트 (발급 수량 증가)
        couponRepository.save(coupon);

        // 6. 쿠폰 발급 이력 생성
        CouponUser couponUser = CouponUser.createIssuedCouponUser(coupon, user);
        couponUser = couponUserRepository.save(couponUser);

        // 7. 결과 반환
        return IssueCouponResult.from(couponUser);

    }

    /**
     * 쿠폰 사용 이력 조회
     */
    @Transactional(readOnly = true)
    public List<CouponUserResult> getCouponUsageHistory(Integer couponId) {
        // 1. 쿠폰 존재 검증
        couponValidator.validateAndGetCoupon(couponId);

        // 2. 쿠폰 사용 이력 조회
        List<CouponUser> couponUsers = couponUserRepository.findByCouponId(couponId);

        // 3. 사용자 이름 조회 및 Result 변환
        return CouponUserResult.fromList(couponUsers, userId -> {
            User user = userRepository.findById(userId);
            return user != null ? user.getUsername() : null;
        });
    }

    /**
     * 초기 쿠폰 데이터 생성
     */
    @Transactional
    public void init() {
        couponRepository.init();
        couponUserRepository.init();
    }
}