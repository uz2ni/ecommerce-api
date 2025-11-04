package com.example.ecommerceapi.presentation.dto;

import com.example.ecommerceapi.point.application.dto.PointResponseDto;
import com.example.ecommerceapi.user.application.dto.UserResponseDto;
import com.example.ecommerceapi.user.presentation.dto.UserResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PointResponse {

    private Integer pointId;
    private Integer userId;
    private String pointType;
    private Integer pointAmount;
    private LocalDateTime createdAt;

    public static PointResponse from(PointResponseDto dto) {
        return PointResponse.builder()
                .pointId(dto.getPointId())
                .userId(dto.getUserId())
                .pointType(dto.getPointType())
                .pointAmount(dto.getPointAmount())
                .createdAt(dto.getCreatedAt())
                .build();
    }

    public static List<PointResponse> fromList(List<PointResponseDto> dtos) {
        return dtos.stream()
                .map(PointResponse::from)
                .toList();
    }
}