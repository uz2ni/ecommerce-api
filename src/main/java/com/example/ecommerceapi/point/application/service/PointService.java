package com.example.ecommerceapi.point.application.service;

import com.example.ecommerceapi.common.aspect.WithLock;
import com.example.ecommerceapi.point.application.dto.PointResult;
import com.example.ecommerceapi.point.domain.entity.Point;
import com.example.ecommerceapi.point.domain.repository.PointRepository;
import com.example.ecommerceapi.user.application.validator.UserValidator;
import com.example.ecommerceapi.user.domain.entity.User;
import com.example.ecommerceapi.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PointService {

    @Value("${constant.point.min-amount}")
    private Integer minAmount;

    @Value("${constant.point.max-amount}")
    private Integer maxAmount;

    private final PointRepository pointRepository;
    private final UserRepository userRepository;
    private final UserValidator userValidator;

    public List<PointResult> getPointHistory(Integer userId) {
        // 사용자 검증
        userValidator.validateAndGetUser(userId);

        return pointRepository.findAllByUserId(userId).stream()
                .map(PointResult::from)
                .collect(Collectors.toList());
    }

    @WithLock(key = "'chargePoint:' + #userId")
    public PointResult chargePoint(Integer userId, Integer amount) {

        // 1. 금액 유효성 검증
        Point.validatePointAmount(amount, minAmount, maxAmount);

        // 2. 사용자 조회 및 검증
        User user = userValidator.validateAndGetUser(userId);

        // 3. User 잔액 변경 & 업데이트
        user.chargePoints(amount);
        userRepository.updateBalance(userId, user.getPointBalance());

        // 4. Point 이력 저장
        Point point = Point.createChargeHistory(userId, amount);
        Point savedPoint = pointRepository.save(point);

        // 5. DTO로 변환하여 반환
        return PointResult.from(savedPoint);
    }
}