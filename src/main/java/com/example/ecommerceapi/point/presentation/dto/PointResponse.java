package com.example.ecommerceapi.point.presentation.dto;

import com.example.ecommerceapi.point.application.dto.PointResult;

import java.time.LocalDateTime;
import java.util.List;

public record PointResponse(
        Integer pointId,
        Integer userId,
        String pointType,
        Integer pointAmount,
        LocalDateTime createdAt
) {
    public static PointResponse from(PointResult dto) {
        return new PointResponse(
                dto.pointId(),
                dto.userId(),
                dto.pointType(),
                dto.pointAmount(),
                dto.createdAt()
        );
    }

    public static List<PointResponse> fromList(List<PointResult> dtos) {
        return dtos.stream()
                .map(PointResponse::from)
                .toList();
    }
}