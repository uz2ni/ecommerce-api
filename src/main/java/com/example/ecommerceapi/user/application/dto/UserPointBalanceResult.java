package com.example.ecommerceapi.user.application.dto;

public record UserPointBalanceResult(
        Integer userId,
        Integer pointBalance
) {
    public static UserPointBalanceResult from(Integer userId, Integer pointBalance) {
        return new UserPointBalanceResult(
                userId,
                pointBalance
        );
    }
}
