package com.example.ecommerceapi.application.service.mockService;

import com.example.ecommerceapi.application.dto.user.PointResponse;
import com.example.ecommerceapi.application.dto.user.UserPointBalanceResponse;
import com.example.ecommerceapi.application.dto.user.UserResponse;
import com.example.ecommerceapi.application.usecase.UserUseCase;
import com.example.ecommerceapi.infrastructure.memory.MockPointData;
import com.example.ecommerceapi.infrastructure.memory.MockUserData;
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