package com.example.ecommerceapi.user.presentation.dto;

import com.example.ecommerceapi.user.application.dto.UserPointBalanceResult;

public record UserPointBalanceResponse(
        Integer userId,
        Integer pointBalance
) {
    public static UserPointBalanceResponse from(UserPointBalanceResult dto) {
        return new UserPointBalanceResponse(
                dto.userId(),
                dto.pointBalance()
        );
    }
}
