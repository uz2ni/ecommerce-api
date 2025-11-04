package com.example.ecommerceapi.domain.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Point 엔티티 단위 테스트")
class PointUnitTest {

    @Nested
    @DisplayName("createChargeHistory 테스트")
    class CreateChargeHistoryTest {

        @Test
        @DisplayName("충전 이력 생성 성공")
        void createChargeHistory_Success() {
            // given
            Integer userId = 1;
            Integer amount = 5000;

            // when
            Point point = Point.createChargeHistory(userId, amount);

            // then
            assertNotNull(point);
            assertEquals(userId, point.getUserId());
            assertEquals(amount, point.getPointAmount());
            assertEquals(PointType.CHARGE, point.getPointType());
            assertNotNull(point.getCreatedAt());
            assertNull(point.getPointId()); // ID는 저장 후 할당됨
        }

        @Test
        @DisplayName("서로 다른 시간에 생성된 이력의 createdAt이 다름")
        void createChargeHistory_DifferentCreatedAt() throws InterruptedException {
            // given
            Integer userId = 1;
            Integer amount = 5000;

            // when
            Point point1 = Point.createChargeHistory(userId, amount);
            Thread.sleep(10); // 약간의 시간 차이를 주기 위해
            Point point2 = Point.createChargeHistory(userId, amount);

            // then
            assertNotEquals(point1.getCreatedAt(), point2.getCreatedAt());
        }
    }

    @Nested
    @DisplayName("createUseHistory 테스트")
    class CreateUseHistoryTest {

        @Test
        @DisplayName("사용 이력 생성 성공")
        void createUseHistory_Success() {
            // given
            Integer userId = 1;
            Integer amount = 3000;

            // when
            Point point = Point.createUseHistory(userId, amount);

            // then
            assertNotNull(point);
            assertEquals(userId, point.getUserId());
            assertEquals(amount, point.getPointAmount());
            assertEquals(PointType.USE, point.getPointType());
            assertNotNull(point.getCreatedAt());
            assertNull(point.getPointId());
        }

        @Test
        @DisplayName("여러 사용자의 사용 이력 생성")
        void createUseHistory_MultipleUsers() {
            // given
            Integer userId1 = 1;
            Integer userId2 = 2;
            Integer amount = 1000;

            // when
            Point point1 = Point.createUseHistory(userId1, amount);
            Point point2 = Point.createUseHistory(userId2, amount);

            // then
            assertNotEquals(point1.getUserId(), point2.getUserId());
            assertEquals(point1.getPointAmount(), point2.getPointAmount());
            assertEquals(PointType.USE, point1.getPointType());
            assertEquals(PointType.USE, point2.getPointType());
        }
    }

    @Nested
    @DisplayName("createRefundHistory 테스트")
    class CreateRefundHistoryTest {

        @Test
        @DisplayName("환불 이력 생성 성공")
        void createRefundHistory_Success() {
            // given
            Integer userId = 1;
            Integer amount = 2000;

            // when
            Point point = Point.createRefundHistory(userId, amount);

            // then
            assertNotNull(point);
            assertEquals(userId, point.getUserId());
            assertEquals(amount, point.getPointAmount());
            assertEquals(PointType.REFUND, point.getPointType());
            assertNotNull(point.getCreatedAt());
            assertNull(point.getPointId());
        }

        @Test
        @DisplayName("환불 금액이 0인 경우도 이력 생성 가능")
        void createRefundHistory_ZeroAmount() {
            // given
            Integer userId = 1;
            Integer amount = 0;

            // when
            Point point = Point.createRefundHistory(userId, amount);

            // then
            assertNotNull(point);
            assertEquals(amount, point.getPointAmount());
            assertEquals(PointType.REFUND, point.getPointType());
        }
    }

    @Nested
    @DisplayName("PointType별 이력 비교 테스트")
    class PointTypeComparisonTest {

        @Test
        @DisplayName("각 타입별 이력이 올바르게 구분됨")
        void differentPointTypes() {
            // given
            Integer userId = 1;
            Integer amount = 1000;

            // when
            Point chargePoint = Point.createChargeHistory(userId, amount);
            Point usePoint = Point.createUseHistory(userId, amount);
            Point refundPoint = Point.createRefundHistory(userId, amount);

            // then
            assertEquals(PointType.CHARGE, chargePoint.getPointType());
            assertEquals(PointType.USE, usePoint.getPointType());
            assertEquals(PointType.REFUND, refundPoint.getPointType());

            assertNotEquals(chargePoint.getPointType(), usePoint.getPointType());
            assertNotEquals(usePoint.getPointType(), refundPoint.getPointType());
            assertNotEquals(chargePoint.getPointType(), refundPoint.getPointType());
        }
    }
}