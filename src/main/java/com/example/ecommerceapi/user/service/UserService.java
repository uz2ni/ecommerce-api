package com.example.ecommerceapi.user.service;

import com.example.ecommerceapi.user.dto.UserPointBalanceResponse;
import com.example.ecommerceapi.user.dto.UserResponse;
import com.example.ecommerceapi.user.mapper.UserMapper;
import com.example.ecommerceapi.user.usecase.UserUseCase;
import com.example.ecommerceapi.user.entity.User;
import com.example.ecommerceapi.user.repository.UserRepository;
import com.example.ecommerceapi.point.validator.PointValidator;
import com.example.ecommerceapi.user.validator.UserValidator;
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
    private final PointValidator pointValidator;

    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
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
        return userMapper.toUserPointBalanceResponse(userId, userRepository.findBalanceById(userId));
    }

    @Override
    @WithLock(key = "'chargePoints:' + #userId", ignoreIfLocked = true)
    public Boolean chargePoints(Integer userId, Integer amount) {
        // 1. 금액 유효성 검증
        pointValidator.validatePointAmount(amount);

        // 2. 사용자 조회 및 검증
        User user = userValidator.validateAndGetUser(userId);

        // 3. 도메인 로직 실행 (잔액 계산)
        user.chargePoints(amount);

        // 4. 변경된 잔액 저장
        userRepository.updateBalance(userId, user.getPointBalance());

        return true;
    }
}