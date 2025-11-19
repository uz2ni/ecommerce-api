package com.example.ecommerceapi.point.application.service;

import com.example.ecommerceapi.common.exception.ErrorCode;
import com.example.ecommerceapi.common.exception.PointException;
import com.example.ecommerceapi.common.exception.UserException;
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
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
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
     * 포인트 충전 (낙관적 락 사용)
     */
    @Transactional
    public PointResult chargePoint(Integer userId, Integer amount) {

        // 1. 금액 유효성 검증
        Point.validatePointAmount(amount, minAmount, maxAmount);

        // 2. 사용자 조회 (낙관적 락 적용)
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new UserException(ErrorCode.USER_NOT_FOUND);
        }

        // 3. 잔여 포인트 반영. 낙관적 락 적용 부분만 재시도
        chargePointsWithRetry(user, amount);

        // 4. Point 이력 저장
        Point point = Point.createChargeHistory(user, amount);
        Point savedPoint = pointRepository.save(point);

        // 5. DTO로 변환하여 반환
        return PointResult.from(savedPoint, user.getPointBalance());
    }

    @Retryable(
            retryFor = {ObjectOptimisticLockingFailureException.class, jakarta.persistence.OptimisticLockException.class},
            exclude = {UserException.class}, // UserException은 재시도 제외
            maxAttempts = 5,
            backoff = @Backoff(delay = 100)
    )
    @Transactional
    public void chargePointsWithRetry(User user, Integer amount) {
        user.chargePoints(amount);
        userRepository.save(user);
    }

    @Transactional
    public void init() {
        // 1. 테이블 초기화
        pointTableUtils.resetPointTable();

        // 2. 샘플 데이터 삽입
        pointRepository.init();
    }

    @Recover
    public PointResult recoverOptimistic(ObjectOptimisticLockingFailureException e,
                                         Integer userId,
                                         Integer amount) {
        // 실패에 대한 보상 로직 (예: 로그만 남기기 or 실패 응답 반환)
        log.debug(">> 재시도 실패, userId: " + userId + ", amount: " + amount);
        log.debug(e.getMessage());
        throw new PointException(ErrorCode.POINT_RACE_CONDITION);
    }
}