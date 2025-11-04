package com.example.ecommerceapi.application.service;

import com.example.ecommerceapi.application.dto.user.PointResponse;
import com.example.ecommerceapi.application.mapper.PointMapper;
import com.example.ecommerceapi.application.usecase.PointUseCase;
import com.example.ecommerceapi.application.usecase.UserUseCase;
import com.example.ecommerceapi.common.exception.ErrorCode;
import com.example.ecommerceapi.common.exception.PointException;
import com.example.ecommerceapi.domain.entity.Point;
import com.example.ecommerceapi.domain.repository.PointRepository;
import com.example.ecommerceapi.domain.service.UserValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PointService implements PointUseCase {

    private final UserUseCase userUseCase;
    private final PointRepository pointRepository;
    private final PointMapper pointMapper;
    private final UserValidator userValidator;

    @Override
    public List<PointResponse> getPointHistory(Integer userId) {
        // 사용자 검증
        userValidator.validateAndGetUser(userId);

        return pointRepository.findAllByUserId(userId).stream()
                .map(pointMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public PointResponse chargePoint(Integer userId, Integer amount) {
        // 1. 사용자 검증 및 잔액 업데이트 (도메인 로직 위임)
        Boolean charged = userUseCase.chargePoints(userId, amount);
        if (!charged) {
            throw new PointException(ErrorCode.POINT_CHARGE_IN_PROGRESS);
        }

        // 2. 포인트 이력 저장 (도메인 팩토리 메서드 사용)
        Point point = Point.createChargeHistory(userId, amount);
        Point savedPoint = pointRepository.save(point);

        // 3. DTO로 변환하여 반환
        return pointMapper.toResponse(savedPoint);
    }
}