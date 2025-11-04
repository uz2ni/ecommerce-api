package com.example.ecommerceapi.point.usecase;

import com.example.ecommerceapi.point.dto.PointResponse;

import java.util.List;

public interface PointUseCase {

    List<PointResponse> getPointHistory(Integer userId);

    PointResponse chargePoint(Integer userId, Integer amount);
}