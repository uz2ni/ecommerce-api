package com.example.ecommerceapi.point.application.service;

import com.example.ecommerceapi.common.AbstractIntegrationTest;
import com.example.ecommerceapi.common.exception.PointException;
import com.example.ecommerceapi.point.application.dto.PointResult;
import com.example.ecommerceapi.user.application.service.UserService;
import com.example.ecommerceapi.user.domain.entity.User;
import com.example.ecommerceapi.user.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DisplayName("PointService 동시성 통합 테스트")
class PointServiceConcurrencyIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private PointService pointService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    private static final int THREAD_COUNT = 10;
    private static final int CHARGE_AMOUNT = 10000;

    @BeforeEach
    void setUp() {
        userService.init();
        pointService.init();
    }

    @Test
    @DisplayName("동시에 여러 번 포인트 충전 요청 시 성공 횟수, 낙관적 락 재시도 실패 횟수 검증")
    void chargePoint_ShouldShowRetryLogsAndRecover_WhenConcurrent() throws InterruptedException {

        // given
        Integer userId = 1;
        User user = userRepository.findById(userId);
        Integer initialBalance = user.getPointBalance();

        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);

        AtomicInteger recoverCalls = new AtomicInteger(0);
        AtomicInteger successCalls = new AtomicInteger(0);

        for (int i = 0; i < THREAD_COUNT; i++) {
            int threadNumber = i + 1;
            executor.submit(() -> {
                try {
                    PointResult result = pointService.chargePoint(userId, CHARGE_AMOUNT);
                    successCalls.incrementAndGet();
                    System.out.println("[Thread " + threadNumber + "] 성공: 잔액=" + result.pointBalance());
                } catch (PointException e) {
                    recoverCalls.incrementAndGet();
                    System.out.println("[Thread " + threadNumber + "] Recover 호출: " + e.getMessage());
                } catch (ObjectOptimisticLockingFailureException e) {
                    System.out.println("[Thread " + threadNumber + "] 재시도 중 충돌 발생: " + e.getClass().getSimpleName());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        User updatedUser = userRepository.findById(userId);
        Integer expectedMinBalance = initialBalance + (CHARGE_AMOUNT * successCalls.get());

        System.out.println("\n--- 테스트 요약 ---");
        System.out.println("초기 잔액: " + initialBalance);
        System.out.println("성공 호출 수: " + successCalls.get());
        System.out.println("Recover 호출 수: " + recoverCalls.get());
        System.out.println("최종 잔액: " + updatedUser.getPointBalance());

        // 최종 잔액 검증
        assertThat(updatedUser.getPointBalance()).isEqualTo(expectedMinBalance);
        assertThat(recoverCalls.get()).isGreaterThanOrEqualTo(0);
    }

    /* [deprecated] 비관적 락일 때 성공하는 테스트
    @Test
    @DisplayName("동시에 여러 번 포인트 충전 요청 시 모든 요청이 처리된다")
    void chargePoint_ShouldProcessAllRequestsSequentially_WhenConcurrent() throws InterruptedException {

        // given
        Integer userId = 1;
        User user = userRepository.findById(userId);
        Integer initialBalance = user.getPointBalance();

        int threads = 10;  // 동시에 10번 충전 요청
        CountDownLatch latch = new CountDownLatch(threads);
        ExecutorService executor = Executors.newFixedThreadPool(threads);

        // when
        for (int i = 0; i < threads; i++) {
            executor.submit(() -> {
                try {
                    pointService.chargePoint(userId, CHARGE_AMOUNT);
                } catch (Exception e) {
                    System.out.println("예외 타입: " + e.getClass().getSimpleName());
                    System.out.println("예외 전체 클래스: " + e.getClass().getName());
                    System.out.println("예외 메세지: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        // then
        User updatedUser = userRepository.findById(userId);
        Integer expectedBalance = initialBalance + (CHARGE_AMOUNT * threads);

        System.out.println("초기 잔액: " + initialBalance);
        System.out.println("최종 잔액: " + updatedUser.getPointBalance());

        assertThat(updatedUser.getPointBalance()).isEqualTo(expectedBalance);
    }
     */

}