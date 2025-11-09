package com.example.ecommerceapi.user.presentation.dto;

import com.example.ecommerceapi.user.application.dto.UserPointBalanceResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPointBalanceResponse {
    private Integer userId;
    private Integer pointBalance;

    public static UserPointBalanceResponse from(UserPointBalanceResult dto) {
        return UserPointBalanceResponse.builder()
                .userId(dto.getUserId())
                .pointBalance(dto.getPointBalance())
                .build();
    }
}
