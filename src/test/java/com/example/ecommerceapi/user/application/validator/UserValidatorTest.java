package com.example.ecommerceapi.user.application.validator;

import com.example.ecommerceapi.common.exception.UserException;
import com.example.ecommerceapi.user.domain.entity.User;
import com.example.ecommerceapi.user.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserValidator 단위 테스트")
class UserValidatorTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserValidator userValidator;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .userId(1)
                .username("testUser")
                .pointBalance(10000)
                .build();
    }

    @Test
    @DisplayName("사용자가 존재하면 User 엔티티를 반환한다")
    void validateAndGetUser_ShouldReturnUser_WhenUserExists() {
        // given
        given(userRepository.findById(1)).willReturn(testUser);

        // when
        User result = userValidator.validateAndGetUser(1);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(1);
        assertThat(result.getUsername()).isEqualTo("testUser");
        assertThat(result.getPointBalance()).isEqualTo(10000);
    }

    @Test
    @DisplayName("사용자가 존재하지 않으면 예외가 발생한다")
    void validateAndGetUser_ShouldThrowException_WhenUserNotFound() {
        // given
        given(userRepository.findById(999)).willReturn(null);

        // when & then
        assertThatThrownBy(() -> userValidator.validateAndGetUser(999))
                .isInstanceOf(UserException.class)
                .hasMessage("회원이 존재하지 않습니다.");
    }
}