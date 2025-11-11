package com.example.ecommerceapi.point.application.service;

import com.example.ecommerceapi.common.AbstractIntegrationTest;
import com.example.ecommerceapi.user.application.service.UserService;
import com.example.ecommerceapi.user.domain.entity.User;
import com.example.ecommerceapi.user.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    @DisplayName("동시에 여러 번 포인트 충전 요청 시 모든 요청이 순차 처리된다")
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


}