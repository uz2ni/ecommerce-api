package com.example.ecommerceapi.point.application.service;

import com.example.ecommerceapi.point.application.dto.PointResult;
import com.example.ecommerceapi.point.domain.entity.Point;
import com.example.ecommerceapi.point.domain.repository.PointRepository;
import com.example.ecommerceapi.point.infrastructure.persistence.PointTableUtils;
import com.example.ecommerceapi.user.application.validator.UserValidator;
import com.example.ecommerceapi.user.domain.entity.User;
import com.example.ecommerceapi.user.domain.repository.UserRepository;
import com.example.ecommerceapi.user.infrastructure.persistence.JpaUserRepositoryAdapter;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class PointService {

    @Value("${constant.point.min-amount}")
    private Integer minAmount;

    @Value("${constant.point.max-amount}")
    private Integer maxAmount;

    private final PointRepository pointRepository;
    private final UserRepository userRepository;
    private final UserValidator userValidator;
    private final PointTableUtils pointTableUtils;

    public List<PointResult> getPointHistory(Integer userId) {
        // 사용자 검증
        User user = userValidator.validateAndGetUser(userId);

        return pointRepository.findAllByUserId(userId).stream()
                .map(point -> PointResult.from(point, user.getPointBalance()))
                .collect(Collectors.toList());
    }

    public PointResult chargePoint(Integer userId, Integer amount) {

        // 1. 금액 유효성 검증
        Point.validatePointAmount(amount, minAmount, maxAmount);

        // 2. 사용자 조회 (비관적 락 적용)
        User user = userValidator.validateAndGetUserWithLock(userId);

        // 3. User 잔액 변경 & 업데이트
        user.chargePoints(amount);
        userRepository.save(user);

        // 4. Point 이력 저장
        Point point = Point.createChargeHistory(user, amount);
        Point savedPoint = pointRepository.save(point);

        // 5. DTO로 변환하여 반환
        return PointResult.from(savedPoint, user.getPointBalance());
    }

    public void init() {
        // 1. 테이블 초기화
        pointTableUtils.resetPointTable();

        // 2. 샘플 데이터 삽입
        pointRepository.init();
    }
}