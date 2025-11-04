package com.example.ecommerceapi.application.usecase;

import com.example.ecommerceapi.application.dto.user.PointResponse;

import java.util.List;

public interface PointUseCase {

    List<PointResponse> getPointHistory(Integer userId);

    PointResponse chargePoint(Integer userId, Integer amount);
}