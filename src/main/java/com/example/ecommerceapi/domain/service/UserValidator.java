package com.example.ecommerceapi.domain.service;

import com.example.ecommerceapi.domain.entity.User;
import com.example.ecommerceapi.domain.repository.UserRepository;
import com.example.ecommerceapi.common.exception.ErrorCode;
import com.example.ecommerceapi.common.exception.UserException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserValidator {

    private final UserRepository userRepository;

    /**
     * 사용자 존재 여부를 검증하고 User 엔티티를 반환합니다.
     * @param userId 검증할 사용자 ID
     * @return User 엔티티
     * @throws UserException 사용자가 존재하지 않을 경우
     */
    public User validateAndGetUser(Integer userId) {
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new UserException(ErrorCode.USER_NOT_FOUND);
        }
        return user;
    }
}