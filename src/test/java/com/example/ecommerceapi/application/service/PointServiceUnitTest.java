package com.example.ecommerceapi.application.service;

import com.example.ecommerceapi.application.dto.user.PointResponse;
import com.example.ecommerceapi.application.mapper.PointMapper;
import com.example.ecommerceapi.application.usecase.UserUseCase;
import com.example.ecommerceapi.common.exception.ErrorCode;
import com.example.ecommerceapi.common.exception.PointException;
import com.example.ecommerceapi.common.exception.UserException;
import com.example.ecommerceapi.domain.entity.Point;
import com.example.ecommerceapi.domain.entity.PointType;
import com.example.ecommerceapi.domain.repository.PointRepository;
import com.example.ecommerceapi.domain.service.UserValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PointService 단위 테스트")
class PointServiceUnitTest {

    @Mock
    private UserUseCase userUseCase;

    @Mock
    private PointRepository pointRepository;

    @Mock
    private PointMapper pointMapper;

    @Mock
    private UserValidator userValidator;

    @InjectMocks
    private PointService pointService;

    private Point mockPoint;
    private PointResponse mockPointResponse;

    @BeforeEach
    void setUp() {
        mockPoint = Point.builder()
                .pointId(1)
                .userId(1)
                .pointType(PointType.CHARGE)
                .pointAmount(5000)
                .createdAt(LocalDateTime.now())
                .build();

        mockPointResponse = PointResponse.builder()
                .pointId(1)
                .userId(1)
                .pointType("CHARGE")
                .pointAmount(5000)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("getPointHistory 테스트")
    class GetPointHistoryTest {

        @Test
        @DisplayName("포인트 이력 조회 성공")
        void getPointHistory_Success() {
            // given
            Integer userId = 1;
            Point point2 = Point.builder()
                    .pointId(2)
                    .userId(1)
                    .pointType(PointType.USE)
                    .pointAmount(3000)
                    .createdAt(LocalDateTime.now())
                    .build();

            PointResponse response2 = PointResponse.builder()
                    .pointId(2)
                    .userId(1)
                    .pointType("USE")
                    .pointAmount(3000)
                    .createdAt(LocalDateTime.now())
                    .build();

            when(userValidator.validateAndGetUser(userId)).thenReturn(any());
            when(pointRepository.findAllByUserId(userId)).thenReturn(Arrays.asList(mockPoint, point2));
            when(pointMapper.toResponse(mockPoint)).thenReturn(mockPointResponse);
            when(pointMapper.toResponse(point2)).thenReturn(response2);

            // when
            List<PointResponse> result = pointService.getPointHistory(userId);

            // then
            assertEquals(2, result.size());
            assertEquals(mockPointResponse, result.get(0));
            assertEquals(response2, result.get(1));
            verify(userValidator, times(1)).validateAndGetUser(userId);
            verify(pointRepository, times(1)).findAllByUserId(userId);
            verify(pointMapper, times(2)).toResponse(any(Point.class));
        }

        @Test
        @DisplayName("포인트 이력이 없는 경우 빈 리스트 반환")
        void getPointHistory_EmptyList() {
            // given
            Integer userId = 1;
            when(userValidator.validateAndGetUser(userId)).thenReturn(any());
            when(pointRepository.findAllByUserId(userId)).thenReturn(Arrays.asList());

            // when
            List<PointResponse> result = pointService.getPointHistory(userId);

            // then
            assertTrue(result.isEmpty());
            verify(userValidator, times(1)).validateAndGetUser(userId);
            verify(pointRepository, times(1)).findAllByUserId(userId);
        }

        @Test
        @DisplayName("존재하지 않는 사용자의 이력 조회 시 예외 발생")
        void getPointHistory_UserNotFound_ThrowsException() {
            // given
            Integer userId = 999;
            when(userValidator.validateAndGetUser(userId))
                    .thenThrow(new UserException(ErrorCode.USER_NOT_FOUND));

            // when & then
            assertThrows(UserException.class, () -> {
                pointService.getPointHistory(userId);
            });
            verify(userValidator, times(1)).validateAndGetUser(userId);
            verify(pointRepository, never()).findAllByUserId(any());
        }
    }

    @Nested
    @DisplayName("chargePoint 테스트")
    class ChargePointTest {

        @Test
        @DisplayName("포인트 충전 성공")
        void chargePoint_Success() {
            // given
            Integer userId = 1;
            Integer amount = 5000;

            when(userUseCase.chargePoints(userId, amount)).thenReturn(true);
            when(pointRepository.save(any(Point.class))).thenReturn(mockPoint);
            when(pointMapper.toResponse(mockPoint)).thenReturn(mockPointResponse);

            // when
            PointResponse result = pointService.chargePoint(userId, amount);

            // then
            assertNotNull(result);
            assertEquals(userId, result.getUserId());
            assertEquals(amount, result.getPointAmount());
            assertEquals("CHARGE", result.getPointType());
            verify(userUseCase, times(1)).chargePoints(userId, amount);
            verify(pointRepository, times(1)).save(any(Point.class));
            verify(pointMapper, times(1)).toResponse(mockPoint);
        }

        @Test
        @DisplayName("포인트 충전 중복 요청 시 예외 발생")
        void chargePoint_InProgress_ThrowsException() {
            // given
            Integer userId = 1;
            Integer amount = 5000;

            when(userUseCase.chargePoints(userId, amount)).thenReturn(false);

            // when & then
            PointException exception = assertThrows(PointException.class, () -> {
                pointService.chargePoint(userId, amount);
            });
            assertEquals(ErrorCode.POINT_CHARGE_IN_PROGRESS, exception.getErrorCode());
            verify(userUseCase, times(1)).chargePoints(userId, amount);
            verify(pointRepository, never()).save(any());
        }

        @Test
        @DisplayName("존재하지 않는 사용자 충전 시 예외 발생")
        void chargePoint_UserNotFound_ThrowsException() {
            // given
            Integer userId = 999;
            Integer amount = 5000;

            when(userUseCase.chargePoints(userId, amount))
                    .thenThrow(new UserException(ErrorCode.USER_NOT_FOUND));

            // when & then
            assertThrows(UserException.class, () -> {
                pointService.chargePoint(userId, amount);
            });
            verify(userUseCase, times(1)).chargePoints(userId, amount);
            verify(pointRepository, never()).save(any());
        }

        @Test
        @DisplayName("유효하지 않은 충전 금액으로 예외 발생")
        void chargePoint_InvalidAmount_ThrowsException() {
            // given
            Integer userId = 1;
            Integer amount = -1000;

            when(userUseCase.chargePoints(userId, amount))
                    .thenThrow(new PointException(ErrorCode.POINT_INVALID_AMOUNT));

            // when & then
            assertThrows(PointException.class, () -> {
                pointService.chargePoint(userId, amount);
            });
            verify(userUseCase, times(1)).chargePoints(userId, amount);
            verify(pointRepository, never()).save(any());
        }

        @Test
        @DisplayName("포인트 이력 저장 확인")
        void chargePoint_SavesHistory() {
            // given
            Integer userId = 1;
            Integer amount = 5000;

            when(userUseCase.chargePoints(userId, amount)).thenReturn(true);
            when(pointRepository.save(any(Point.class))).thenAnswer(invocation -> {
                Point savedPoint = invocation.getArgument(0);
                assertEquals(userId, savedPoint.getUserId());
                assertEquals(amount, savedPoint.getPointAmount());
                assertEquals(PointType.CHARGE, savedPoint.getPointType());
                assertNotNull(savedPoint.getCreatedAt());
                return savedPoint;
            });
            when(pointMapper.toResponse(any(Point.class))).thenReturn(mockPointResponse);

            // when
            pointService.chargePoint(userId, amount);

            // then
            verify(pointRepository, times(1)).save(any(Point.class));
        }
    }
}