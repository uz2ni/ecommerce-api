package com.example.ecommerceapi.coupon.application.service;

import com.example.ecommerceapi.common.lock.DistributedLock;
import com.example.ecommerceapi.common.lock.LockType;
import com.example.ecommerceapi.common.redis.CacheType;
import com.example.ecommerceapi.common.exception.CouponException;
import com.example.ecommerceapi.common.exception.ErrorCode;
import com.example.ecommerceapi.coupon.application.dto.CouponResult;
import com.example.ecommerceapi.coupon.application.dto.CouponUserResult;
import com.example.ecommerceapi.coupon.application.dto.IssueCouponCommand;
import com.example.ecommerceapi.coupon.application.dto.IssueCouponResult;
import com.example.ecommerceapi.coupon.application.validator.CouponValidator;
import com.example.ecommerceapi.coupon.domain.entity.Coupon;
import com.example.ecommerceapi.coupon.domain.entity.CouponUser;
import com.example.ecommerceapi.coupon.domain.event.CouponIssuePublisher;
import com.example.ecommerceapi.coupon.domain.repository.CouponRepository;
import com.example.ecommerceapi.coupon.domain.repository.CouponUserRepository;
import com.example.ecommerceapi.user.application.validator.UserValidator;
import com.example.ecommerceapi.user.domain.entity.User;
import com.example.ecommerceapi.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
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
    private final CouponIssuePublisher couponIssuePublisher;

    /**
     * 쿠폰 정보 목록 조회
     */
    @Cacheable(value = CacheType.Names.ALL_COUPONS)
    @Transactional(readOnly = true)
    public List<CouponResult> getAllCoupons() {
        List<Coupon> coupons = couponRepository.findAll();
        return CouponResult.fromList(coupons);
    }

    /**
     * 쿠폰 발급 (동기 방식)
     * - 선착순으로 쿠폰을 발급
     * - 중복 발급 불가
     * - 발급 수량이 소진되면 실패
     * - 쿠폰이 만료되면 실패
     * - 분산 락을 통한 동시성 제어
     * - 즉시 발급 처리 후 결과 반환
     */
    @DistributedLock(key = "'coupon:' + #command.couponId", type = LockType.PUB_SUB, waitTime = 5, leaseTime = 10)
    @Transactional
    public IssueCouponResult issueCoupon(IssueCouponCommand command) {
        // 1. 회원 존재 검증
        User user = userValidator.validateAndGetUser(command.userId());

        // 2. 쿠폰 존재 검증
        Coupon coupon = couponRepository.findById(command.couponId())
                .orElseThrow(() -> new CouponException(ErrorCode.COUPON_NOT_FOUND));

        // 3. 쿠폰 만료 검증
        coupon.validateNotExpired();

        // 4. 중복 발급 검증
        Optional<CouponUser> existingCouponUser = couponUserRepository
                .findByCouponIdAndUserId(command.couponId(), command.userId());
        if (existingCouponUser.isPresent()) {
            throw new CouponException(ErrorCode.COUPON_ALREADY_ISSUED);
        }

        // 5. 쿠폰 발급 가능 여부 검증 및 발급 수량 증가
        coupon.issueCoupon();

        // 6. 쿠폰 업데이트
        couponRepository.save(coupon);

        // 7. 쿠폰 발급 이력 생성
        CouponUser couponUser = CouponUser.createIssuedCouponUser(coupon, user);
        CouponUser savedCouponUser = couponUserRepository.saveAndFlush(couponUser);

        // 8. 발급 완료 응답 반환
        return IssueCouponResult.from(savedCouponUser);
    }

    /**
     * 쿠폰 발급 접수 (비동기 방식)
     * - 선착순으로 쿠폰을 발급
     * - 중복 발급 불가
     * - 발급 수량이 소진되면 실패
     * - 쿠폰이 만료되면 실패
     * <Redis Stream 메시지 큐>
     * - 쿠폰 발급 요청을 Redis Stream에 발행
     * - 실제 발급 처리는 CouponEventConsumer에서 비동기로 수행
     */
    @Transactional
    public IssueCouponResult issueCouponAsync(IssueCouponCommand command) {
        // 1. 기본 검증: 회원 존재 여부
        User user = userValidator.validateAndGetUser(command.userId());

        // 2. 기본 검증: 쿠폰 존재 여부
        Coupon coupon = couponRepository.findById(command.couponId())
                .orElseThrow(() -> new CouponException(ErrorCode.COUPON_NOT_FOUND));

        // 3. 기본 검증: 쿠폰 발급 가능 여부 (쿠폰 만료 여부, 쿠폰 수량 확인)
        coupon.validIssueCoupon();

        // 4. 쿠폰 발급 이벤트 발행 (비동기 처리를 위해)
        String eventId = couponIssuePublisher.publish(
                command.couponId(),
                command.userId()
        );

        // 5. 요청 접수 응답 반환 (실제 발급은 비동기 처리)
        return IssueCouponResult.pending(
                command.couponId(),
                command.userId(),
                eventId
        );
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
     * 쿠폰 발급 이벤트 처리 (Consumer에서 호출)
     * - Stream Consumer에서 비동기로 호출되는 실제 쿠폰 발급 로직
     * - 트랜잭션 내에서 발급 수량 증가 및 발급 이력 생성
     *
     * @param couponId 쿠폰 ID
     * @param userId 사용자 ID
     */
    @Transactional
    public void processCouponIssue(Integer couponId, Integer userId) {
        // 1. 회원 존재 검증
        User user = userValidator.validateAndGetUser(userId);

        // 2. 쿠폰 존재 검증
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new CouponException(ErrorCode.COUPON_NOT_FOUND));

        // 3. 중복 발급 검증
        Optional<CouponUser> existingCouponUser = couponUserRepository
                .findByCouponIdAndUserId(couponId, userId);
        if (existingCouponUser.isPresent()) {
            throw new CouponException(ErrorCode.COUPON_ALREADY_ISSUED);
        }

        // 4. 쿠폰 발급 가능 여부 검증 및 발급 수량 증가
        coupon.issueCoupon();

        // 5. 쿠폰 업데이트
        couponRepository.save(coupon);

        // 6. 쿠폰 발급 이력 생성
        CouponUser couponUser = CouponUser.createIssuedCouponUser(coupon, user);
        couponUserRepository.save(couponUser);
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