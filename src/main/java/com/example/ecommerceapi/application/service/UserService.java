package com.example.ecommerceapi.application.service;

import com.example.ecommerceapi.application.dto.user.UserPointBalanceResponse;
import com.example.ecommerceapi.application.dto.user.UserResponse;
import com.example.ecommerceapi.application.mapper.UserMapper;
import com.example.ecommerceapi.application.usecase.UserUseCase;
import com.example.ecommerceapi.domain.entity.User;
import com.example.ecommerceapi.domain.repository.UserRepository;
import com.example.ecommerceapi.domain.service.UserValidator;
import com.example.ecommerceapi.common.aspect.WithLock;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Primary
@RequiredArgsConstructor
public class UserService implements UserUseCase {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final UserValidator userValidator;

    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.getAllUsers().stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public UserResponse getUser(Integer userId) {
        User user = userValidator.validateAndGetUser(userId);
        return userMapper.toResponse(user);
    }

    @Override
    public UserPointBalanceResponse getPointBalance(Integer userId) {
        User user = userValidator.validateAndGetUser(userId);
        return userMapper.toUserPointBalanceResponse(userId, userRepository.getBalance(userId));
    }

    @Override
    @WithLock(key = "'chargePoints:' + #userId", ignoreIfLocked = true)
    public Boolean chargePoints(Integer userId, Integer amount) {
        // 1. 사용자 조회 및 검증
        User user = userValidator.validateAndGetUser(userId);

        // 2. 도메인 로직 실행 (검증 + 잔액 계산)
        user.chargePoints(amount);

        // 3. 변경된 잔액 저장
        userRepository.updateBalance(userId, user.getPointBalance());

        return true;
    }
}