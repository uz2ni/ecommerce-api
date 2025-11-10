package com.example.ecommerceapi.user.application.dto;

import com.example.ecommerceapi.user.domain.entity.User;

public record UserResult(
        Integer userId,
        String username,
        Integer pointBalance
) {
    public static UserResult from(User user) {
        return new UserResult(
                user.getUserId(),
                user.getUsername(),
                user.getPointBalance()
        );
    }
}
