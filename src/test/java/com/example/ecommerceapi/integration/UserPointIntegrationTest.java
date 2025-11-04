package com.example.ecommerceapi.integration;

import com.example.ecommerceapi.application.dto.user.PointResponse;
import com.example.ecommerceapi.application.dto.user.UserPointBalanceResponse;
import com.example.ecommerceapi.application.service.PointService;
import com.example.ecommerceapi.application.service.UserService;
import com.example.ecommerceapi.common.exception.PointException;
import com.example.ecommerceapi.domain.entity.Point;
import com.example.ecommerceapi.domain.entity.PointType;
import com.example.ecommerceapi.domain.entity.User;
import com.example.ecommerceapi.domain.repository.PointRepository;
import com.example.ecommerceapi.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
        "constant.point.min-amount=1000",
        "constant.point.max-amount=1000000"
})
@DisplayName("User-Point 통합 테스트")
class UserPointIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private PointService pointService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PointRepository pointRepository;

    private User testUser;
    private Integer testUserId;

    @BeforeEach
    void setUp() {
        // 테스트용 사용자 ID (기존 데이터 사용)
        testUserId = 1;
        testUser = userRepository.findById(testUserId);
    }

    @Nested
    @DisplayName("포인트 충전 통합 테스트")
    class ChargePointIntegrationTest {

        @Test
        @DisplayName("포인트 충전 시 User 잔액 증가 및 Point 이력 생성")
        void chargePoint_UpdatesBalanceAndCreatesHistory() {
            // given
            Integer chargeAmount = 5000;
            Integer initialBalance = userRepository.findBalanceById(testUserId);
            int initialHistoryCount = pointRepository.findAllByUserId(testUserId).size();

            // when
            PointResponse response = pointService.chargePoint(testUserId, chargeAmount);

            // then - 잔액 확인
            Integer updatedBalance = userRepository.findBalanceById(testUserId);
            assertEquals(initialBalance + chargeAmount, updatedBalance);

            // then - 이력 확인
            List<Point> history = pointRepository.findAllByUserId(testUserId);
            assertEquals(initialHistoryCount + 1, history.size());

            // then - 최신 이력의 타입과 금액 확인
            Point latestHistory = history.get(history.size() - 1);
            assertEquals(PointType.CHARGE, latestHistory.getPointType());
            assertEquals(chargeAmount, latestHistory.getPointAmount());
            assertEquals(testUserId, latestHistory.getUserId());
            assertNotNull(latestHistory.getCreatedAt());

            // then - 응답 확인
            assertNotNull(response);
            assertEquals(testUserId, response.getUserId());
            assertEquals(chargeAmount, response.getPointAmount());
            assertEquals(PointType.CHARGE, response.getPointType());
        }

        @Test
        @DisplayName("최소 금액(1,000원) 충전 - 경계값")
        void chargePoint_MinAmount_Success() {
            // given
            Integer chargeAmount = 1000; // 최소 금액
            Integer initialBalance = userRepository.findBalanceById(testUserId);

            // when
            PointResponse response = pointService.chargePoint(testUserId, chargeAmount);

            // then
            Integer updatedBalance = userRepository.findBalanceById(testUserId);
            assertEquals(initialBalance + chargeAmount, updatedBalance);
            assertNotNull(response);
        }

        @Test
        @DisplayName("최대 금액(1,000,000원) 충전 - 경계값")
        void chargePoint_MaxAmount_Success() {
            // given
            Integer chargeAmount = 1000000; // 최대 금액
            Integer initialBalance = userRepository.findBalanceById(testUserId);

            // when
            PointResponse response = pointService.chargePoint(testUserId, chargeAmount);

            // then
            Integer updatedBalance = userRepository.findBalanceById(testUserId);
            assertEquals(initialBalance + chargeAmount, updatedBalance);
            assertNotNull(response);
        }

        @Test
        @DisplayName("최소 금액 미만(999원) 충전 시 예외 발생 - 경계값")
        void chargePoint_BelowMinAmount_ThrowsException() {
            // given
            Integer chargeAmount = 999; // 최소 금액 - 1
            Integer initialBalance = userRepository.findBalanceById(testUserId);
            int initialHistoryCount = pointRepository.findAllByUserId(testUserId).size();

            // when & then
            assertThrows(PointException.class, () -> {
                pointService.chargePoint(testUserId, chargeAmount);
            });

            // then - 잔액 변화 없음
            Integer updatedBalance = userRepository.findBalanceById(testUserId);
            assertEquals(initialBalance, updatedBalance);

            // then - 이력 생성 안됨
            int updatedHistoryCount = pointRepository.findAllByUserId(testUserId).size();
            assertEquals(initialHistoryCount, updatedHistoryCount);
        }

        @Test
        @DisplayName("최대 금액 초과(1,000,001원) 충전 시 예외 발생 - 경계값")
        void chargePoint_AboveMaxAmount_ThrowsException() {
            // given
            Integer chargeAmount = 1000001; // 최대 금액 + 1
            Integer initialBalance = userRepository.findBalanceById(testUserId);
            int initialHistoryCount = pointRepository.findAllByUserId(testUserId).size();

            // when & then
            assertThrows(PointException.class, () -> {
                pointService.chargePoint(testUserId, chargeAmount);
            });

            // then - 잔액 변화 없음
            Integer updatedBalance = userRepository.findBalanceById(testUserId);
            assertEquals(initialBalance, updatedBalance);

            // then - 이력 생성 안됨
            int updatedHistoryCount = pointRepository.findAllByUserId(testUserId).size();
            assertEquals(initialHistoryCount, updatedHistoryCount);
        }

        @Test
        @DisplayName("null 금액 충전 시 예외 발생")
        void chargePoint_NullAmount_ThrowsException() {
            // given
            Integer chargeAmount = null;
            Integer initialBalance = userRepository.findBalanceById(testUserId);
            int initialHistoryCount = pointRepository.findAllByUserId(testUserId).size();

            // when & then
            assertThrows(PointException.class, () -> {
                pointService.chargePoint(testUserId, chargeAmount);
            });

            // then - 잔액 변화 없음
            Integer updatedBalance = userRepository.findBalanceById(testUserId);
            assertEquals(initialBalance, updatedBalance);

            // then - 이력 생성 안됨
            int updatedHistoryCount = pointRepository.findAllByUserId(testUserId).size();
            assertEquals(initialHistoryCount, updatedHistoryCount);
        }

        @Test
        @DisplayName("여러 번 충전 시 잔액과 이력이 누적된다")
        void chargePoint_Multiple_Accumulates() {
            // given
            Integer firstCharge = 5000;
            Integer secondCharge = 3000;
            Integer thirdCharge = 7000;
            Integer initialBalance = userRepository.findBalanceById(testUserId);
            int initialHistoryCount = pointRepository.findAllByUserId(testUserId).size();

            // when
            pointService.chargePoint(testUserId, firstCharge);
            pointService.chargePoint(testUserId, secondCharge);
            pointService.chargePoint(testUserId, thirdCharge);

            // then - 잔액 누적 확인
            Integer updatedBalance = userRepository.findBalanceById(testUserId);
            assertEquals(initialBalance + firstCharge + secondCharge + thirdCharge, updatedBalance);

            // then - 이력 개수 확인
            List<Point> history = pointRepository.findAllByUserId(testUserId);
            assertEquals(initialHistoryCount + 3, history.size());

            // then - 모든 이력이 CHARGE 타입인지 확인
            List<Point> newHistories = history.subList(initialHistoryCount, history.size());
            assertTrue(newHistories.stream().allMatch(h -> h.getPointType() == PointType.CHARGE));
        }
    }

    @Nested
    @DisplayName("포인트 조회 통합 테스트")
    class GetPointIntegrationTest {

        @Test
        @DisplayName("포인트 충전 후 잔액 조회")
        void getPointBalance_AfterCharge() {
            // given
            Integer chargeAmount = 10000;
            Integer initialBalance = userRepository.findBalanceById(testUserId);

            // when
            pointService.chargePoint(testUserId, chargeAmount);
            UserPointBalanceResponse response = userService.getPointBalance(testUserId);

            // then
            assertEquals(testUserId, response.getUserId());
            assertEquals(initialBalance + chargeAmount, response.getPointBalance());
        }

        @Test
        @DisplayName("포인트 충전 후 이력 조회")
        void getPointHistory_AfterCharge() {
            // given
            Integer chargeAmount = 8000;
            int initialHistoryCount = pointRepository.findAllByUserId(testUserId).size();

            // when
            pointService.chargePoint(testUserId, chargeAmount);
            List<com.example.ecommerceapi.application.dto.user.PointResponse> history =
                    pointService.getPointHistory(testUserId);

            // then
            assertEquals(initialHistoryCount + 1, history.size());

            // 최신 이력 확인
            com.example.ecommerceapi.application.dto.user.PointResponse latestHistory =
                    history.get(history.size() - 1);
            assertEquals(testUserId, latestHistory.getUserId());
            assertEquals(PointType.CHARGE, latestHistory.getPointType());
            assertEquals(chargeAmount, latestHistory.getPointAmount());
        }
    }
}