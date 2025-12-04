package com.example.ecommerceapi.coupon.infrastructure.stream;

import com.example.ecommerceapi.common.AbstractIntegrationTest;
import com.example.ecommerceapi.coupon.application.dto.IssueCouponCommand;
import com.example.ecommerceapi.coupon.application.dto.IssueCouponResult;
import com.example.ecommerceapi.coupon.application.service.CouponService;
import com.example.ecommerceapi.coupon.domain.entity.Coupon;
import com.example.ecommerceapi.coupon.domain.entity.CouponUser;
import com.example.ecommerceapi.coupon.domain.repository.CouponRepository;
import com.example.ecommerceapi.coupon.domain.repository.CouponUserRepository;
import com.example.ecommerceapi.user.domain.entity.User;
import com.example.ecommerceapi.user.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@Testcontainers
@DisplayName("쿠폰 발급 Redis Stream 통합 테스트")
class CouponStreamIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private CouponService couponService;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private CouponUserRepository couponUserRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        // 테스트 데이터 초기화
        couponUserRepository.init();
        couponRepository.init();
    }

    @Test
    @DisplayName("쿠폰 발급 요청이 Redis Stream을 통해 비동기로 처리된다")
    void issueCouponViaStream() throws InterruptedException {
        // given
        User user = userRepository.findById(3);
        Coupon coupon = couponRepository.findById(3).orElseThrow();
        Integer initialIssuedQuantity = coupon.getIssuedQuantity();

        IssueCouponCommand command = new IssueCouponCommand(3, 3);

        // when
        IssueCouponResult result = couponService.issueCouponAsync(command);

        // then - 즉시 PENDING 상태로 응답
        assertThat(result.status()).isEqualTo("PENDING");
        assertThat(result.eventId()).isNotNull();
        assertThat(result.couponUserId()).isNull();

        // 비동기 처리 대기 (최대 5초)
        await().atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    // Consumer가 처리한 후 쿠폰 발급 이력 확인
                    List<CouponUser> couponUsers = couponUserRepository.findByCouponIdAndUserId(3, 3)
                            .map(List::of)
                            .orElse(List.of());

                    assertThat(couponUsers).hasSize(1);
                    assertThat(couponUsers.get(0).getCoupon().getCouponId()).isEqualTo(3);
                    assertThat(couponUsers.get(0).getUser().getUserId()).isEqualTo(3);

                    // 쿠폰 발급 수량 증가 확인
                    Coupon updatedCoupon = couponRepository.findById(1).orElseThrow();
                    assertThat(updatedCoupon.getIssuedQuantity()).isEqualTo(initialIssuedQuantity + 1);
                });
    }

    @Test
    @DisplayName("동시에 100명이 쿠폰 발급 요청 시 선착순으로 처리된다")
    void concurrentCouponIssue() throws InterruptedException {
        // given - 수량이 50개인 쿠폰 생성
        Coupon coupon = Coupon.builder()
                .couponName("선착순 쿠폰")
                .discountAmount(5000)
                .totalQuantity(50)
                .issuedQuantity(0)
                .expiredAt(LocalDateTime.now().plusDays(7))
                .createdAt(LocalDateTime.now())
                .version(0)
                .build();
        coupon = couponRepository.save(coupon);
        Integer testCouponId = coupon.getCouponId();

        // 100명의 사용자 생성
        int threadCount = 100;
        for (int i = 1; i <= threadCount; i++) {
            User user = User.builder()
                    .username("testUser" + i)
                    .pointBalance(10000)
                    .version(0)
                    .build();
            userRepository.save(user);
        }
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // when - 100명이 동시에 쿠폰 발급 요청
        for (int i = 1; i <= threadCount; i++) {
            int userId = i;
            executorService.submit(() -> {
                try {
                    IssueCouponCommand command = new IssueCouponCommand(userId, testCouponId);
                    couponService.issueCouponAsync(command);
                } catch (Exception e) {
                    // 예외 발생 시 로그만 남김 (만료, 소진 등)
                    System.out.println("Error for user " + userId + ": " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // then - 비동기 처리 대기 (최대 10초)
        await().atMost(10, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    // 정확히 50개만 발급되었는지 확인
                    List<CouponUser> issuedCoupons = couponUserRepository.findByCouponId(testCouponId);
                    assertThat(issuedCoupons).hasSize(50);

                    // 쿠폰 발급 수량 확인
                    Coupon updatedCoupon = couponRepository.findById(testCouponId).orElseThrow();
                    assertThat(updatedCoupon.getIssuedQuantity()).isEqualTo(50);
                    assertThat(updatedCoupon.getRemainingQuantity()).isEqualTo(0);
                });
    }

    @Test
    @DisplayName("중복 발급 요청 시 한 번만 발급된다")
    void duplicateIssuePrevention() throws InterruptedException {
        // given
        User user = userRepository.findById(3);
        Coupon coupon = couponRepository.findById(3).orElseThrow();
        IssueCouponCommand command = new IssueCouponCommand(3, 3);

        // when - 같은 사용자가 3번 요청
        couponService.issueCouponAsync(command);
        couponService.issueCouponAsync(command);
        couponService.issueCouponAsync(command);

        // then - 비동기 처리 대기 후 1개만 발급 확인
        await().atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    List<CouponUser> couponUsers = couponUserRepository.findByCouponId(3);
                    long count = couponUsers.stream()
                            .filter(cu -> cu.getUser().getUserId().equals(3))
                            .count();
                    assertThat(count).isEqualTo(1);
                });
    }
}
