package com.example.ecommerceapi.user.application.service;

import com.example.ecommerceapi.user.infrastructure.InMemoryUserRepository;
import com.example.ecommerceapi.user.application.dto.UserPointBalanceResult;
import com.example.ecommerceapi.user.application.dto.UserResult;
import com.example.ecommerceapi.user.domain.entity.User;
import com.example.ecommerceapi.user.application.validator.UserValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Primary
@RequiredArgsConstructor
public class UserService {

    private final InMemoryUserRepository userRepository;
    private final UserValidator userValidator;

    public List<UserResult> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserResult::from)
                .collect(Collectors.toList());
    }

    public UserResult getUser(Integer userId) {
        User user = userValidator.validateAndGetUser(userId);
        return UserResult.from(user);
    }

    public UserPointBalanceResult getPointBalance(Integer userId) {
        userValidator.validateAndGetUser(userId);
        Integer balance = userRepository.findBalanceById(userId);
        return UserPointBalanceResult.from(userId, balance);
    }

}