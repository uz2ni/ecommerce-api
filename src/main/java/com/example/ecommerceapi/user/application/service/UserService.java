package com.example.ecommerceapi.user.application.service;

import com.example.ecommerceapi.user.infrastructure.InMemoryUserRepository;
import com.example.ecommerceapi.user.application.dto.UserPointBalanceResponseDto;
import com.example.ecommerceapi.user.application.dto.UserResponseDto;
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

    public List<UserResponseDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserResponseDto::from)
                .collect(Collectors.toList());
    }

    public UserResponseDto getUser(Integer userId) {
        User user = userValidator.validateAndGetUser(userId);
        return UserResponseDto.from(user);
    }

    public UserPointBalanceResponseDto getPointBalance(Integer userId) {
        userValidator.validateAndGetUser(userId);
        Integer balance = userRepository.findBalanceById(userId);
        return UserPointBalanceResponseDto.from(userId, balance);
    }

}