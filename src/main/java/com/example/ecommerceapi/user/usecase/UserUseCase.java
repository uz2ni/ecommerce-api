package com.example.ecommerceapi.user.usecase;

import com.example.ecommerceapi.user.dto.UserPointBalanceResponse;
import com.example.ecommerceapi.user.dto.UserResponse;

import java.util.List;

public interface UserUseCase {

    List<UserResponse> getAllUsers();

    UserResponse getUser(Integer userId);

    UserPointBalanceResponse getPointBalance(Integer userId);

    /**
     * 사용자의 포인트를 충전합니다.
     * @param userId 사용자 ID
     * @param amount 충전할 금액
     */
    Boolean chargePoints(Integer userId, Integer amount);
}