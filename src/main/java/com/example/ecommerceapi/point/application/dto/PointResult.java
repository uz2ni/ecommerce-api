package com.example.ecommerceapi.point.application.dto;

import com.example.ecommerceapi.point.domain.entity.Point;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PointResult {

    private Integer pointId;
    private Integer userId;
    private String pointType;
    private Integer pointAmount;
    private LocalDateTime createdAt;

    public static PointResult from(Point point) {
        return PointResult.builder()
                .pointId(point.getPointId())
                .userId(point.getUserId())
                .pointType(point.getPointType().name())
                .pointAmount(point.getPointAmount())
                .createdAt(point.getCreatedAt())
                .build();
    }
}