package com.example.ecommerceapi.user.presentation.dto;

import com.example.ecommerceapi.user.application.dto.UserResult;

import java.util.List;

public record UserResponse(
        Integer userId,
        String username,
        Integer pointBalance
) {
    public static UserResponse from(UserResult dto) {
        return new UserResponse(
                dto.userId(),
                dto.username(),
                dto.pointBalance()
        );
    }

    public static List<UserResponse> fromList(List<UserResult> dtos) {
        return dtos.stream()
                .map(UserResponse::from)
                .toList();
    }
}
