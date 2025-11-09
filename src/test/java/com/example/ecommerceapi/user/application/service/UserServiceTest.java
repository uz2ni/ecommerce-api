package com.example.ecommerceapi.user.application.service;

import com.example.ecommerceapi.common.exception.UserException;
import com.example.ecommerceapi.user.application.dto.UserPointBalanceResult;
import com.example.ecommerceapi.user.application.dto.UserResult;
import com.example.ecommerceapi.user.application.validator.UserValidator;
import com.example.ecommerceapi.user.domain.entity.User;
import com.example.ecommerceapi.user.infrastructure.InMemoryUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService 단위 테스트")
class UserServiceTest {

    @Mock
    private InMemoryUserRepository userRepository;

    @Mock
    private UserValidator userValidator;

    @InjectMocks
    private UserService userService;

    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        user1 = User.builder()
                .userId(1)
                .username("user1")
                .pointBalance(10000)
                .build();

        user2 = User.builder()
                .userId(2)
                .username("user2")
                .pointBalance(20000)
                .build();
    }

    @Test
    @DisplayName("모든 사용자 목록을 조회한다")
    void getAllUsers_ShouldReturnAllUsers() {
        // given
        List<User> users = Arrays.asList(user1, user2);
        given(userRepository.findAll()).willReturn(users);

        // when
        List<UserResult> result = userService.getAllUsers();

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getUserId()).isEqualTo(1);
        assertThat(result.get(0).getUsername()).isEqualTo("user1");
        assertThat(result.get(0).getPointBalance()).isEqualTo(10000);
        assertThat(result.get(1).getUserId()).isEqualTo(2);
        assertThat(result.get(1).getUsername()).isEqualTo("user2");
        assertThat(result.get(1).getPointBalance()).isEqualTo(20000);
    }

    @Test
    @DisplayName("사용자가 없으면 빈 목록을 반환한다")
    void getAllUsers_ShouldReturnEmptyList_WhenNoUsers() {
        // given
        given(userRepository.findAll()).willReturn(Arrays.asList());

        // when
        List<UserResult> result = userService.getAllUsers();

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("사용자 ID로 사용자를 조회한다")
    void getUser_ShouldReturnUser_WhenUserExists() {
        // given
        given(userValidator.validateAndGetUser(1)).willReturn(user1);

        // when
        UserResult result = userService.getUser(1);

        // then
        assertThat(result.getUserId()).isEqualTo(1);
        assertThat(result.getUsername()).isEqualTo("user1");
        assertThat(result.getPointBalance()).isEqualTo(10000);
    }

    @Test
    @DisplayName("존재하지 않는 사용자를 조회하면 예외가 발생한다")
    void getUser_ShouldThrowException_WhenUserNotFound() {
        // given
        willThrow(new UserException(com.example.ecommerceapi.common.exception.ErrorCode.USER_NOT_FOUND))
                .given(userValidator).validateAndGetUser(999);

        // when & then
        assertThatThrownBy(() -> userService.getUser(999))
                .isInstanceOf(UserException.class)
                .hasMessage("회원이 존재하지 않습니다.");
    }

    @Test
    @DisplayName("사용자의 포인트 잔액을 조회한다")
    void getPointBalance_ShouldReturnBalance_WhenUserExists() {
        // given
        given(userValidator.validateAndGetUser(1)).willReturn(user1);
        given(userRepository.findBalanceById(1)).willReturn(10000);

        // when
        UserPointBalanceResult result = userService.getPointBalance(1);

        // then
        assertThat(result.getUserId()).isEqualTo(1);
        assertThat(result.getPointBalance()).isEqualTo(10000);
    }

    @Test
    @DisplayName("존재하지 않는 사용자의 포인트 잔액 조회 시 예외가 발생한다")
    void getPointBalance_ShouldThrowException_WhenUserNotFound() {
        // given
        willThrow(new UserException(com.example.ecommerceapi.common.exception.ErrorCode.USER_NOT_FOUND))
                .given(userValidator).validateAndGetUser(999);

        // when & then
        assertThatThrownBy(() -> userService.getPointBalance(999))
                .isInstanceOf(UserException.class)
                .hasMessage("회원이 존재하지 않습니다.");
    }
}
