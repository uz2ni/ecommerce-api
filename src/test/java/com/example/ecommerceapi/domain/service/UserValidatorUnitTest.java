package com.example.ecommerceapi.domain.service;

import com.example.ecommerceapi.common.exception.ErrorCode;
import com.example.ecommerceapi.common.exception.UserException;
import com.example.ecommerceapi.domain.entity.User;
import com.example.ecommerceapi.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserValidator 단위 테스트")
class UserValidatorUnitTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserValidator userValidator;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = User.builder()
                .userId(1)
                .username("testUser")
                .pointBalance(10000)
                .build();
    }

    @Test
    @DisplayName("존재하는 사용자 검증 성공")
    void validateAndGetUser_Success() {
        // given
        Integer userId = 1;
        when(userRepository.findById(userId)).thenReturn(mockUser);

        // when
        User result = userValidator.validateAndGetUser(userId);

        // then
        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals("testUser", result.getUsername());
        assertEquals(10000, result.getPointBalance());
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    @DisplayName("존재하지 않는 사용자 검증 시 예외 발생")
    void validateAndGetUser_UserNotFound_ThrowsException() {
        // given
        Integer userId = 999;
        when(userRepository.findById(userId)).thenReturn(null);

        // when & then
        UserException exception = assertThrows(UserException.class, () -> {
            userValidator.validateAndGetUser(userId);
        });
        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    @DisplayName("null 사용자 ID로 검증 시 예외 발생")
    void validateAndGetUser_NullUserId_ThrowsException() {
        // given
        Integer userId = null;
        when(userRepository.findById(userId)).thenReturn(null);

        // when & then
        UserException exception = assertThrows(UserException.class, () -> {
            userValidator.validateAndGetUser(userId);
        });
        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    @DisplayName("여러 번 검증해도 매번 조회 수행")
    void validateAndGetUser_MultipleCalls() {
        // given
        Integer userId = 1;
        when(userRepository.findById(userId)).thenReturn(mockUser);

        // when
        User result1 = userValidator.validateAndGetUser(userId);
        User result2 = userValidator.validateAndGetUser(userId);

        // then
        assertNotNull(result1);
        assertNotNull(result2);
        verify(userRepository, times(2)).findById(userId);
    }
}