package com.example.ecommerceapi.point.application.dto;

import com.example.ecommerceapi.point.domain.entity.Point;

import java.time.LocalDateTime;

public record PointResult(
        Integer pointId,
        Integer userId,
        String pointType,
        Integer pointAmount,
        Integer pointBalance,
        LocalDateTime createdAt
) {
    public static PointResult from(Point point, Integer pointBalance) {
        return new PointResult(
                point.getPointId(),
                point.getUserId(),
                point.getPointType().name(),
                point.getPointAmount(),
                pointBalance,
                point.getCreatedAt()
        );
    }
}