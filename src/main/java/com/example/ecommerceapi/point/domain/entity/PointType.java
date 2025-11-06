package com.example.ecommerceapi.point.domain.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PointType {
    CHARGE("충전"),
    USE("사용"),
    REFUND("환불");

    private final String description;
}