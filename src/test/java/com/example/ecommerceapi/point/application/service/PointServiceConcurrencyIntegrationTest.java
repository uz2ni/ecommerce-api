package com.example.ecommerceapi.point.application.service;

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
class PointServiceConcurrencyIntegrationTest {

    @Autowired
    private PointService pointService;

    @Autowired
    private UserRepository userRepository;

    private static final int THREAD_COUNT = 10;
    private static final int CHARGE_AMOUNT = 10000;

    @BeforeEach
    void setUp() {
        userRepository.clear();
        userRepository.init();
    }

    @Test
    @DisplayName("동시에 여러 번 포인트 충전 요청 시 최초 요청만 처리된다")
    void chargePoint_ShouldProcessOnlyFirstRequest_WhenConcurrent() throws InterruptedException {
        // given
        Integer userId = 1;
        User user = userRepository.findById(userId);
        Integer initialBalance = user.getPointBalance(); // init에서 세팅된 500000

        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);

        // when: 동시에 여러 요청
        for (int i = 0; i < THREAD_COUNT; i++) {
            executorService.submit(() -> {
                try {
                    pointService.chargePoint(userId, CHARGE_AMOUNT);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // then: 최초 1건만 처리
        User updatedUser = userRepository.findById(userId);
        Integer expectedBalance = initialBalance + CHARGE_AMOUNT; // 500000 + 10000 = 510000

        assertThat(updatedUser.getPointBalance()).isEqualTo(expectedBalance);
        System.out.println("초기 잔액: " + initialBalance);
        System.out.println("최종 잔액: " + updatedUser.getPointBalance());
    }


}