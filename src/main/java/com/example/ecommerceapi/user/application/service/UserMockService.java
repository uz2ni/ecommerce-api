package com.example.ecommerceapi.user.application.service;

import com.example.ecommerceapi.user.presentation.dto.UserPointBalanceResponse;
import com.example.ecommerceapi.user.presentation.dto.UserResponse;
import com.example.ecommerceapi.user.infrastructure.MockUserData;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserMockService {

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

    public Boolean chargePoints(Integer userId, Integer amount) {
        return true;
    }

}