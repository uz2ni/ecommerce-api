package com.example.ecommerceapi.user.application.dto;

import com.example.ecommerceapi.user.domain.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDto {

    private Integer userId;
    private String username;
    private Integer pointBalance;

    public static UserResponseDto from(User user) {
        return UserResponseDto.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .pointBalance(user.getPointBalance())
                .build();
    }
}
