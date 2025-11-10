package com.example.ecommerceapi.coupon.application.service;

import com.example.ecommerceapi.common.exception.CouponException;
import com.example.ecommerceapi.coupon.application.dto.IssueCouponCommand;
import com.example.ecommerceapi.coupon.domain.entity.Coupon;
import com.example.ecommerceapi.coupon.domain.entity.CouponUser;
import com.example.ecommerceapi.coupon.domain.repository.CouponRepository;
import com.example.ecommerceapi.coupon.domain.repository.CouponUserRepository;
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
class CouponServiceConcurrencyIntegrationTest {

    @Autowired
    private CouponService couponService;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private CouponUserRepository couponUserRepository;

    private static final int THREAD_COUNT = 20;

    @BeforeEach
    void setUp() {
        // 각 테스트 전에 초기 상태로 리셋
        couponRepository.clear();
        couponUserRepository.clear();
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
                    IssueCouponCommand command = new IssueCouponCommand(userId, 2);
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
                    IssueCouponCommand command = new IssueCouponCommand(finalUserId, 3);
                    couponService.issueCoupon(command);
                    successCount.incrementAndGet();
                    System.out.println("success: " + successCount.get() + ", latchCnt: " + latch.getCount());
                } catch (CouponException e) {
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

        System.out.println("성공한 발급 수: " + successCount.get());
        System.out.println("최종 발급 수량: " + updatedCoupon.getIssuedQuantity());
    }

}
