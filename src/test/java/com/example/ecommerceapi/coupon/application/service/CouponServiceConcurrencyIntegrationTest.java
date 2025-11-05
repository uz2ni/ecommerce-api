package com.example.ecommerceapi.coupon.application.service;

import com.example.ecommerceapi.common.exception.CouponException;
import com.example.ecommerceapi.coupon.application.dto.IssueCouponCommand;
import com.example.ecommerceapi.coupon.domain.entity.Coupon;
import com.example.ecommerceapi.coupon.domain.entity.CouponUser;
import com.example.ecommerceapi.coupon.infrastructure.InMemoryCouponRepository;
import com.example.ecommerceapi.coupon.infrastructure.InMemoryCouponUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DisplayName("CouponService 동시성 통합 테스트")
class CouponServiceConcurrencyIntegrationTest { // TODO: 쿠폰 동시성 테스트 미통과 수정 필요

    @Autowired
    private CouponService couponService;

    @Autowired
    private InMemoryCouponRepository couponRepository;

    @Autowired
    private InMemoryCouponUserRepository couponUserRepository;

    private static final int THREAD_COUNT = 20;

    @BeforeEach
    void setUp() {
        // 각 테스트 전에 초기 상태로 리셋
        couponRepository.clear();
        couponUserRepository.clear();
    }

    @Test
    @DisplayName("동시에 여러 사용자가 쿠폰 발급 요청 시 남은 수량만큼만 발급된다")
    void issueCoupon_ShouldIssueOnlyRemainingQuantity_WhenConcurrent() throws InterruptedException {
        // given: 남은 수량이 10개인 쿠폰 생성
        Coupon coupon = Coupon.builder()
                .couponId(1)
                .couponName("선착순 10명 쿠폰")
                .discountAmount(10000)
                .totalQuantity(10)
                .issuedQuantity(0)
                .expiredAt(LocalDateTime.now().plusDays(30))
                .createdAt(LocalDateTime.now())
                .build();
        couponRepository.save(coupon);

        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // when: 20명의 사용자가 동시에 10개 쿠폰 발급 시도
        for (int userId = 1; userId <= THREAD_COUNT; userId++) {
            final int finalUserId = userId;
            executorService.submit(() -> {
                try {
                    IssueCouponCommand command = IssueCouponCommand.builder()
                            .couponId(1)
                            .userId(finalUserId)
                            .build();
                    couponService.issueCoupon(command);
                    successCount.incrementAndGet();
                } catch (CouponException e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // then: 정확히 10개만 발급되어야 함
        Coupon updatedCoupon = couponRepository.findById(1).orElseThrow();
        assertThat(updatedCoupon.getIssuedQuantity()).isEqualTo(10);
        assertThat(updatedCoupon.getRemainingQuantity()).isEqualTo(0);
        assertThat(successCount.get()).isEqualTo(10);
        assertThat(failCount.get()).isEqualTo(10);

        // 발급 이력도 정확히 10개여야 함
        assertThat(couponUserRepository.count()).isEqualTo(10);

        System.out.println("성공한 발급 수: " + successCount.get());
        System.out.println("실패한 발급 수: " + failCount.get());
        System.out.println("최종 발급 수량: " + updatedCoupon.getIssuedQuantity());
    }

    @Test
    @DisplayName("동시에 동일 사용자가 같은 쿠폰 발급 요청 시 1개만 발급된다")
    void issueCoupon_ShouldIssueOnlyOnce_WhenSameUserConcurrent() throws InterruptedException {
        // given: 쿠폰 생성
        Coupon coupon = Coupon.builder()
                .couponId(2)
                .couponName("중복 방지 테스트 쿠폰")
                .discountAmount(10000)
                .totalQuantity(50)
                .issuedQuantity(0)
                .expiredAt(LocalDateTime.now().plusDays(30))
                .createdAt(LocalDateTime.now())
                .build();
        couponRepository.save(coupon);

        Integer userId = 1;
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // when: 동일 사용자가 동시에 여러 번 발급 시도
        for (int i = 0; i < THREAD_COUNT; i++) {
            executorService.submit(() -> {
                try {
                    IssueCouponCommand command = IssueCouponCommand.builder()
                            .couponId(2)
                            .userId(userId)
                            .build();
                    couponService.issueCoupon(command);
                    successCount.incrementAndGet();
                } catch (CouponException e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // then: 정확히 1개만 발급되어야 함
        Coupon updatedCoupon = couponRepository.findById(2).orElseThrow();
        assertThat(updatedCoupon.getIssuedQuantity()).isEqualTo(1);
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failCount.get()).isEqualTo(THREAD_COUNT - 1);

        // 해당 사용자의 발급 이력도 1개여야 함
        assertThat(couponUserRepository.findByUserId(userId)).hasSize(1);

        System.out.println("성공한 발급 수: " + successCount.get());
        System.out.println("실패한 발급 수: " + failCount.get());
        System.out.println("최종 발급 수량: " + updatedCoupon.getIssuedQuantity());
    }

    @Test
    @DisplayName("동시에 여러 사용자가 마지막 1개 쿠폰 발급 시 1명만 성공한다")
    void issueCoupon_ShouldIssueLastCouponToOneUser_WhenConcurrent() throws InterruptedException {
        // given: 마지막 1개 남은 쿠폰
        Coupon coupon = Coupon.builder()
                .couponId(3)
                .couponName("마지막 1개 쿠폰")
                .discountAmount(10000)
                .totalQuantity(10)
                .issuedQuantity(9)
                .expiredAt(LocalDateTime.now().plusDays(30))
                .createdAt(LocalDateTime.now())
                .build();
        couponRepository.save(coupon);

        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // when: 여러 사용자가 동시에 마지막 1개 발급 시도
        for (int userId = 1; userId <= THREAD_COUNT; userId++) {
            final int finalUserId = userId;
            executorService.submit(() -> {
                try {
                    IssueCouponCommand command = IssueCouponCommand.builder()
                            .couponId(3)
                            .userId(finalUserId)
                            .build();
                    couponService.issueCoupon(command);
                    successCount.incrementAndGet();
                    System.out.println("success: " + successCount.get() + ", latchCnt: " + latch.getCount());
                } catch (CouponException e) {
                    failCount.incrementAndGet();
                    System.out.println("failCount++:" + failCount.get());
                } finally {
                    latch.countDown();
                    System.out.println("latch.countDown():" + latch.getCount());
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // then: 정확히 1개만 발급되어야 함
        Coupon updatedCoupon = couponRepository.findById(3).orElseThrow();
        assertThat(updatedCoupon.getIssuedQuantity()).isEqualTo(10);
        assertThat(updatedCoupon.getRemainingQuantity()).isEqualTo(0);
        assertThat(updatedCoupon.isAvailable()).isFalse();
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failCount.get()).isEqualTo(THREAD_COUNT - 1);

        System.out.println("성공한 발급 수: " + successCount.get());
        System.out.println("실패한 발급 수: " + failCount.get());
        System.out.println("최종 발급 수량: " + updatedCoupon.getIssuedQuantity());
    }

    @Test
    @DisplayName("동시에 여러 사용자가 서로 다른 쿠폰을 발급받을 수 있다")
    void issueCoupon_ShouldIssueDifferentCoupons_WhenConcurrent() throws InterruptedException {
        // given: 여러 쿠폰 생성
        for (int i = 1; i <= 5; i++) {
            Coupon coupon = Coupon.builder()
                    .couponId(i)
                    .couponName("쿠폰 " + i)
                    .discountAmount(10000)
                    .totalQuantity(50)
                    .issuedQuantity(0)
                    .expiredAt(LocalDateTime.now().plusDays(30))
                    .createdAt(LocalDateTime.now())
                    .build();
            couponRepository.save(coupon);
        }

        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
        AtomicInteger successCount = new AtomicInteger(0);

        // when: 여러 사용자가 동시에 서로 다른 쿠폰 발급
        for (int i = 0; i < THREAD_COUNT; i++) {
            final int userId = i + 1;
            final int couponId = (i % 5) + 1; // 1~5 순환
            executorService.submit(() -> {
                try {
                    IssueCouponCommand command = IssueCouponCommand.builder()
                            .couponId(couponId)
                            .userId(userId)
                            .build();
                    couponService.issueCoupon(command);
                    successCount.incrementAndGet();
                } catch (CouponException e) {
                    // 중복 발급 실패 가능
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // then: 모든 발급이 성공해야 함 (각 사용자는 서로 다른 쿠폰이거나, 같은 쿠폰이라도 다른 사용자)
        assertThat(successCount.get()).isEqualTo(THREAD_COUNT);
        assertThat(couponUserRepository.count()).isEqualTo(THREAD_COUNT);

        // 각 쿠폰별 발급 수량 확인
        for (int i = 1; i <= 5; i++) {
            Coupon coupon = couponRepository.findById(i).orElseThrow();
            System.out.println("쿠폰 " + i + " 발급 수량: " + coupon.getIssuedQuantity());
        }
    }
}
