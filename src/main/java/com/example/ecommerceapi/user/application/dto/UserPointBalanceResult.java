package com.example.ecommerceapi.user.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPointBalanceResult {

    private Integer userId;
    private Integer pointBalance;

    public static UserPointBalanceResult from(Integer userId, Integer pointBalance) {
        return UserPointBalanceResult.builder()
                .userId(userId)
                .pointBalance(pointBalance)
                .build();
    }
}
