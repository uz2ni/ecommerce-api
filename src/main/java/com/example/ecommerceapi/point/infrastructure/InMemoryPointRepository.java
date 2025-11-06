package com.example.ecommerceapi.point.infrastructure;

import com.example.ecommerceapi.point.entity.Point;
import com.example.ecommerceapi.point.entity.PointType;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Repository
public class InMemoryPointRepository {

    private final Map<Integer, List<Point>> POINT_HISTORY = new HashMap<>();
    private final AtomicInteger POINT_ID_GENERATOR = new AtomicInteger(1);

    @PostConstruct
    public void init() {
        // 회원별 초기 포인트 충전 이력 생성
        addInitialPoint(1, 500000);
        addInitialPoint(2, 1000000);
        addInitialPoint(3, 300000);
        addInitialPoint(4, 750000);
        addInitialPoint(5, 2000000);
    }

    private void addInitialPoint(Integer userId, Integer amount) {
        Point point = Point.builder()
                .pointId(POINT_ID_GENERATOR.getAndIncrement())
                .userId(userId)
                .pointType(PointType.CHARGE)
                .pointAmount(amount)
                .createdAt(LocalDateTime.now().minusDays(7))
                .build();

        save(point);
    }

    public List<Point> findAllByUserId(Integer userId) {
        return POINT_HISTORY.getOrDefault(userId, Collections.emptyList());
    }

    public Point save(Point point) {
        // pointId가 없으면 자동 생성
        if (point.getPointId() == null) {
            point = Point.builder()
                    .pointId(POINT_ID_GENERATOR.getAndIncrement())
                    .userId(point.getUserId())
                    .pointType(point.getPointType())
                    .pointAmount(point.getPointAmount())
                    .createdAt(point.getCreatedAt())
                    .build();
        }

        List<Point> history = POINT_HISTORY.computeIfAbsent(
                point.getUserId(),
                k -> new ArrayList<>()
        );
        history.add(point);

        return point;
    }

    public void delete(Integer pointId) {
        POINT_HISTORY.remove(pointId);
    }

    public void clear() {
        POINT_HISTORY.clear();
    }
}