package com.example.ecommerceapi.user.application.dto;

import com.example.ecommerceapi.user.presentation.dto.UserPointBalanceResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPointBalanceResponseDto {

    private Integer userId;
    private Integer pointBalance;

    public static UserPointBalanceResponseDto from(Integer userId, Integer pointBalance) {
        return UserPointBalanceResponseDto.builder()
                .userId(userId)
                .pointBalance(pointBalance)
                .build();
    }
}
