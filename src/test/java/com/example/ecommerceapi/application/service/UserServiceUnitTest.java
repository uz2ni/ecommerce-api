package com.example.ecommerceapi.application.service;

import com.example.ecommerceapi.application.dto.user.UserPointBalanceResponse;
import com.example.ecommerceapi.application.dto.user.UserResponse;
import com.example.ecommerceapi.application.mapper.UserMapper;
import com.example.ecommerceapi.common.exception.ErrorCode;
import com.example.ecommerceapi.common.exception.PointException;
import com.example.ecommerceapi.common.exception.UserException;
import com.example.ecommerceapi.domain.entity.User;
import com.example.ecommerceapi.domain.repository.UserRepository;
import com.example.ecommerceapi.domain.service.PointValidator;
import com.example.ecommerceapi.domain.service.UserValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService 단위 테스트")
class UserServiceUnitTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserValidator userValidator;

    @Mock
    private PointValidator pointValidator;

    @InjectMocks
    private UserService userService;

    private User mockUser;
    private UserResponse mockUserResponse;

    @BeforeEach
    void setUp() {
        mockUser = User.builder()
                .userId(1)
                .username("testUser")
                .pointBalance(10000)
                .build();

        mockUserResponse = new UserResponse(1, "testUser", 10000);
    }

    @Nested
    @DisplayName("getAllUsers 테스트")
    class GetAllUsersTest {

        @Test
        @DisplayName("모든 사용자 조회 성공")
        void getAllUsers_Success() {
            // given
            User user2 = User.builder()
                    .userId(2)
                    .username("testUser2")
                    .pointBalance(20000)
                    .build();

            UserResponse response2 = new UserResponse(2, "testUser2", 20000);

            when(userRepository.findAll()).thenReturn(Arrays.asList(mockUser, user2));
            when(userMapper.toResponse(mockUser)).thenReturn(mockUserResponse);
            when(userMapper.toResponse(user2)).thenReturn(response2);

            // when
            List<UserResponse> result = userService.getAllUsers();

            // then
            assertEquals(2, result.size());
            assertEquals(mockUserResponse, result.get(0));
            assertEquals(response2, result.get(1));
            verify(userRepository, times(1)).findAll();
            verify(userMapper, times(2)).toResponse(any(User.class));
        }

        @Test
        @DisplayName("사용자가 없을 때 빈 리스트 반환")
        void getAllUsers_EmptyList() {
            // given
            when(userRepository.findAll()).thenReturn(Arrays.asList());

            // when
            List<UserResponse> result = userService.getAllUsers();

            // then
            assertTrue(result.isEmpty());
            verify(userRepository, times(1)).findAll();
        }
    }

    @Nested
    @DisplayName("getUser 테스트")
    class GetUserTest {

        @Test
        @DisplayName("사용자 조회 성공")
        void getUser_Success() {
            // given
            Integer userId = 1;
            when(userValidator.validateAndGetUser(userId)).thenReturn(mockUser);
            when(userMapper.toResponse(mockUser)).thenReturn(mockUserResponse);

            // when
            UserResponse result = userService.getUser(userId);

            // then
            assertNotNull(result);
            assertEquals(userId, result.getUserId());
            assertEquals("testUser", result.getUsername());
            verify(userValidator, times(1)).validateAndGetUser(userId);
            verify(userMapper, times(1)).toResponse(mockUser);
        }

        @Test
        @DisplayName("존재하지 않는 사용자 조회 시 예외 발생")
        void getUser_UserNotFound_ThrowsException() {
            // given
            Integer userId = 999;
            when(userValidator.validateAndGetUser(userId))
                    .thenThrow(new UserException(ErrorCode.USER_NOT_FOUND));

            // when & then
            assertThrows(UserException.class, () -> {
                userService.getUser(userId);
            });
            verify(userValidator, times(1)).validateAndGetUser(userId);
            verify(userMapper, never()).toResponse(any(User.class));
        }
    }

    @Nested
    @DisplayName("getPointBalance 테스트")
    class GetPointBalanceTest {

        @Test
        @DisplayName("포인트 잔액 조회 성공")
        void getPointBalance_Success() {
            // given
            Integer userId = 1;
            Integer balance = 10000;
            UserPointBalanceResponse mockResponse = new UserPointBalanceResponse(userId, balance);

            when(userValidator.validateAndGetUser(userId)).thenReturn(mockUser);
            when(userRepository.findBalanceById(userId)).thenReturn(balance);
            when(userMapper.toUserPointBalanceResponse(userId, balance)).thenReturn(mockResponse);

            // when
            UserPointBalanceResponse result = userService.getPointBalance(userId);

            // then
            assertNotNull(result);
            assertEquals(userId, result.getUserId());
            assertEquals(balance, result.getPointBalance());
            verify(userValidator, times(1)).validateAndGetUser(userId);
            verify(userRepository, times(1)).findBalanceById(userId);
        }

        @Test
        @DisplayName("존재하지 않는 사용자의 잔액 조회 시 예외 발생")
        void getPointBalance_UserNotFound_ThrowsException() {
            // given
            Integer userId = 999;
            when(userValidator.validateAndGetUser(userId))
                    .thenThrow(new UserException(ErrorCode.USER_NOT_FOUND));

            // when & then
            assertThrows(UserException.class, () -> {
                userService.getPointBalance(userId);
            });
            verify(userValidator, times(1)).validateAndGetUser(userId);
            verify(userRepository, never()).findBalanceById(any());
        }
    }

    @Nested
    @DisplayName("chargePoints 테스트")
    class ChargePointsTest {

        @Test
        @DisplayName("포인트 충전 성공")
        void chargePoints_Success() {
            // given
            Integer userId = 1;
            Integer amount = 5000;
            Integer newBalance = 15000;

            doNothing().when(pointValidator).validatePointAmount(amount);
            when(userValidator.validateAndGetUser(userId)).thenReturn(mockUser);
            doNothing().when(userRepository).updateBalance(userId, newBalance);

            // when
            Boolean result = userService.chargePoints(userId, amount);

            // then
            assertTrue(result);
            assertEquals(newBalance, mockUser.getPointBalance());
            verify(pointValidator, times(1)).validatePointAmount(amount);
            verify(userValidator, times(1)).validateAndGetUser(userId);
            verify(userRepository, times(1)).updateBalance(userId, newBalance);
        }

        @Test
        @DisplayName("존재하지 않는 사용자 충전 시 예외 발생")
        void chargePoints_UserNotFound_ThrowsException() {
            // given
            Integer userId = 999;
            Integer amount = 5000;
            doNothing().when(pointValidator).validatePointAmount(amount);
            when(userValidator.validateAndGetUser(userId))
                    .thenThrow(new UserException(ErrorCode.USER_NOT_FOUND));

            // when & then
            assertThrows(UserException.class, () -> {
                userService.chargePoints(userId, amount);
            });
            verify(pointValidator, times(1)).validatePointAmount(amount);
            verify(userValidator, times(1)).validateAndGetUser(userId);
            verify(userRepository, never()).updateBalance(any(), any());
        }

        @Test
        @DisplayName("유효하지 않은 충전 금액으로 예외 발생")
        void chargePoints_InvalidAmount_ThrowsException() {
            // given
            Integer userId = 1;
            Integer amount = -1000;

            doThrow(new PointException(ErrorCode.POINT_INVALID_AMOUNT))
                    .when(pointValidator).validatePointAmount(amount);

            // when & then
            assertThrows(PointException.class, () -> {
                userService.chargePoints(userId, amount);
            });
            verify(pointValidator, times(1)).validatePointAmount(amount);
            verify(userValidator, never()).validateAndGetUser(any());
            verify(userRepository, never()).updateBalance(any(), any());
        }

        @Test
        @DisplayName("충전 금액이 null인 경우 예외 발생")
        void chargePoints_NullAmount_ThrowsException() {
            // given
            Integer userId = 1;
            Integer amount = null;

            doThrow(new PointException(ErrorCode.POINT_INVALID_AMOUNT))
                    .when(pointValidator).validatePointAmount(amount);

            // when & then
            assertThrows(PointException.class, () -> {
                userService.chargePoints(userId, amount);
            });
            verify(pointValidator, times(1)).validatePointAmount(amount);
            verify(userValidator, never()).validateAndGetUser(any());
            verify(userRepository, never()).updateBalance(any(), any());
        }
    }
}
