package com.example.ecommerceapi.point.infrastructure;

import com.example.ecommerceapi.point.application.dto.PointResponseDto;
import com.example.ecommerceapi.user.infrastructure.MockUserData;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class MockPointData {

    private static final Map<Integer, List<PointResponseDto>> POINT_HISTORY = new HashMap<>();
    private static final AtomicInteger POINT_ID_GENERATOR = new AtomicInteger(1);

    static {
        // 회원별 초기 포인트 충전
        addInitialPoint(1, 500000);
        addInitialPoint(2, 1000000);
        addInitialPoint(3, 300000);
        addInitialPoint(4, 750000);
        addInitialPoint(5, 2000000);
    }

    private static void addInitialPoint(Integer userId, Integer amount) {
        List<PointResponseDto> history = POINT_HISTORY.computeIfAbsent(userId, k -> new ArrayList<>());

        PointResponseDto point = PointResponseDto.builder()
                .pointId(POINT_ID_GENERATOR.getAndIncrement())
                .userId(userId)
                .pointType("EARN")
                .pointAmount(amount)
                .createdAt(LocalDateTime.now().minusDays(7))
                .build();

        history.add(point);
    }

    public static Integer getBalance(Integer userId) {
        return MockUserData.getBalance(userId);
    }

    public static PointResponseDto usePoint(Integer userId, Integer amount) {
        Integer currentBalance = getBalance(userId);

        if (currentBalance < amount) {
            return null; // 잔액 부족
        }

        List<PointResponseDto> history = POINT_HISTORY.computeIfAbsent(userId, k -> new ArrayList<>());
        Integer newBalance = currentBalance - amount;

        PointResponseDto point = PointResponseDto.builder()
                .pointId(POINT_ID_GENERATOR.getAndIncrement())
                .userId(userId)
                .pointType("USE")
                .pointAmount(amount)
                .createdAt(LocalDateTime.now())
                .build();

        history.add(point);
        MockUserData.updateBalance(userId, newBalance);
        return point;
    }

    public static PointResponseDto addPoint(Integer userId, Integer amount) {
        Integer currentBalance = getBalance(userId);
        List<PointResponseDto> history = POINT_HISTORY.computeIfAbsent(userId, k -> new ArrayList<>());
        Integer newBalance = currentBalance + amount;

        PointResponseDto point = PointResponseDto.builder()
                .pointId(POINT_ID_GENERATOR.getAndIncrement())
                .userId(userId)
                .pointType("EARN")
                .pointAmount(amount)
                .createdAt(LocalDateTime.now())
                .build();

        history.add(point);
        MockUserData.updateBalance(userId, newBalance);
        return point;
    }

    public static List<PointResponseDto> getPointHistory(Integer userId) {
        return POINT_HISTORY.getOrDefault(userId, Collections.emptyList());
    }
}