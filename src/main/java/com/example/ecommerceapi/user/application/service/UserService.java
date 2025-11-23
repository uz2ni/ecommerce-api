package com.example.ecommerceapi.user.application.service;

import com.example.ecommerceapi.user.application.dto.UserPointBalanceResult;
import com.example.ecommerceapi.user.application.dto.UserResult;
import com.example.ecommerceapi.user.application.validator.UserValidator;
import com.example.ecommerceapi.user.domain.entity.User;
import com.example.ecommerceapi.user.domain.repository.UserRepository;
import com.example.ecommerceapi.user.infrastructure.persistence.UserTableUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserValidator userValidator;
    private final UserTableUtils userTableUtils;


    @Transactional(readOnly = true)
    public List<UserResult> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserResult::from)
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public UserResult getUser(Integer userId) {
        User user = userValidator.validateAndGetUser(userId);
        return UserResult.from(user);
    }

    @Transactional(readOnly = true)
    public UserPointBalanceResult getPointBalance(Integer userId) {
        userValidator.validateAndGetUser(userId);
        Integer balance = userRepository.findBalanceById(userId);
        return UserPointBalanceResult.from(userId, balance);
    }

    @Transactional
    public void init() {
        // 1. 테이블 초기화
        userTableUtils.resetUserTable();

        // 2. 샘플 데이터 삽입
        userRepository.save(User.builder().username("김철수").pointBalance(500000).version(0).build());
        userRepository.save(User.builder().username("이영희").pointBalance(1000000).version(0).build());
        userRepository.save(User.builder().username("박민수").pointBalance(300000).version(0).build());
        userRepository.save(User.builder().username("정수진").pointBalance(750000).version(0).build());
        userRepository.save(User.builder().username("최동욱").pointBalance(2000000).version(0).build());
        userRepository.save(User.builder().username("김철수2").pointBalance(500000).version(0).build());
        userRepository.save(User.builder().username("이영희2").pointBalance(1000000).version(0).build());
        userRepository.save(User.builder().username("박민수2").pointBalance(300000).version(0).build());
        userRepository.save(User.builder().username("정수진2").pointBalance(750000).version(0).build());
        userRepository.save(User.builder().username("최동욱2").pointBalance(2000000).version(0).build());
        userRepository.save(User.builder().username("김철수3").pointBalance(500000).version(0).build());
        userRepository.save(User.builder().username("이영희3").pointBalance(1000000).version(0).build());
        userRepository.save(User.builder().username("박민수3").pointBalance(300000).version(0).build());
        userRepository.save(User.builder().username("정수진3").pointBalance(750000).version(0).build());
        userRepository.save(User.builder().username("최동욱3").pointBalance(2000000).version(0).build());
        userRepository.save(User.builder().username("김철수4").pointBalance(500000).version(0).build());
        userRepository.save(User.builder().username("이영희4").pointBalance(1000000).version(0).build());
        userRepository.save(User.builder().username("박민수4").pointBalance(300000).version(0).build());
        userRepository.save(User.builder().username("정수진4").pointBalance(750000).version(0).build());
        userRepository.save(User.builder().username("최동욱5").pointBalance(2000000).version(0).build());
    }

}