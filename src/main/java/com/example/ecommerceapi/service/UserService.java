package com.example.ecommerceapi.service;

import com.example.ecommerceapi.dto.user.PointResponse;
import com.example.ecommerceapi.dto.user.UserResponse;
import com.example.ecommerceapi.mock.MockPointData;
import com.example.ecommerceapi.mock.MockUserData;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    public List<UserResponse> getAllUsers() {
        return MockUserData.getAllUsers();
    }

    public UserResponse getUser(Integer userId) {
        return MockUserData.getUser(userId);
    }

    public Integer getPointBalance(Integer userId) {
        return MockPointData.getBalance(userId);
    }

    public List<PointResponse> getPointHistory(Integer userId) {
        return MockPointData.getPointHistory(userId);
    }

    public PointResponse chargePoint(Integer userId, Integer amount) {
        return MockPointData.addPoint(userId, amount);
    }
}