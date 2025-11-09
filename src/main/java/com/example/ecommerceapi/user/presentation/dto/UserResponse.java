package com.example.ecommerceapi.user.presentation.dto;

import com.example.ecommerceapi.user.application.dto.UserResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Integer userId;
    private String username;
    private Integer pointBalance;

    public static UserResponse from(UserResult dto) {
        return UserResponse.builder()
                .userId(dto.getUserId())
                .username(dto.getUsername())
                .pointBalance(dto.getPointBalance())
                .build();
    }

    public static List<UserResponse> fromList(List<UserResult> dtos) {
        return dtos.stream()
                .map(UserResponse::from)
                .toList();
    }
}
