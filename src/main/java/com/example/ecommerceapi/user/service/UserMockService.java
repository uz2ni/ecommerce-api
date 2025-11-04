package com.example.ecommerceapi.user.service;

import com.example.ecommerceapi.user.dto.UserPointBalanceResponse;
import com.example.ecommerceapi.user.dto.UserResponse;
import com.example.ecommerceapi.user.usecase.UserUseCase;
import com.example.ecommerceapi.user.infrastructure.MockUserData;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserMockService implements UserUseCase {

    public List<UserResponse> getAllUsers() {
        return MockUserData.getAllUsers();
    }

    public UserResponse getUser(Integer userId) {
        return MockUserData.getUser(userId);
    }

    public UserPointBalanceResponse getPointBalance(Integer userId) {
        // return MockPointData.getBalance(userId);
        return new UserPointBalanceResponse();
    }

    @Override
    public Boolean chargePoints(Integer userId, Integer amount) {
        return true;
    }

}