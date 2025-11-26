package com.example.ecommerceapi.point.application.service;

import com.example.ecommerceapi.common.exception.ErrorCode;
import com.example.ecommerceapi.common.exception.UserException;
import com.example.ecommerceapi.common.lock.DistributedLock;
import com.example.ecommerceapi.common.lock.LockType;
import com.example.ecommerceapi.point.application.dto.PointResult;
import com.example.ecommerceapi.point.domain.entity.Point;
import com.example.ecommerceapi.point.domain.repository.PointRepository;
import com.example.ecommerceapi.point.infrastructure.persistence.PointTableUtils;
import com.example.ecommerceapi.user.application.validator.UserValidator;
import com.example.ecommerceapi.user.domain.entity.User;
import com.example.ecommerceapi.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
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
    private final PointTableUtils pointTableUtils;

    @Transactional(readOnly = true)
    public List<PointResult> getPointHistory(Integer userId) {
        // 사용자 검증
        User user = userValidator.validateAndGetUser(userId);

        return pointRepository.findAllByUserId(userId).stream()
                .map(point -> PointResult.from(point, user.getPointBalance()))
                .collect(Collectors.toList());
    }

    /**
     * 포인트 충전
     * <분산 락-PUB_SUB>
     * point:#userId  // 포인트 충전 중복 제어
     */
    @DistributedLock(key = "'point:' + #userId", type = LockType.SIMPLE)
    @Transactional
    public PointResult chargePoint(Integer userId, Integer amount) {

        // 1. 금액 유효성 검증
        Point.validatePointAmount(amount, minAmount, maxAmount);

        // 2. 사용자 조회
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new UserException(ErrorCode.USER_NOT_FOUND);
        }

        // 3. 잔여 포인트 반영
        user.chargePoints(amount);
        userRepository.save(user);

        // 4. Point 이력 저장
        Point point = Point.createChargeHistory(user, amount);
        Point savedPoint = pointRepository.save(point);

        // 5. DTO로 변환하여 반환
        return PointResult.from(savedPoint, user.getPointBalance());
    }

    @Transactional
    public void init() {
        // 1. 테이블 초기화
        pointTableUtils.resetPointTable();

        // 2. 샘플 데이터 삽입
        pointRepository.init();
    }
}