package com.example.ecommerceapi.point.infrastructure.memory;

import com.example.ecommerceapi.point.domain.entity.Point;
import com.example.ecommerceapi.point.domain.entity.PointType;
import com.example.ecommerceapi.point.domain.repository.PointRepository;
import com.example.ecommerceapi.user.domain.entity.User;
import com.example.ecommerceapi.user.domain.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * InMemory 기반 PointRepository 구현체
 */
@Repository
@RequiredArgsConstructor
public class InMemoryPointRepository implements PointRepository {

    private final Map<Integer, List<Point>> POINT_HISTORY = new HashMap<>();
    private final AtomicInteger POINT_ID_GENERATOR = new AtomicInteger(1);
    private final UserRepository userRepository;

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
        User user = userRepository.findById(userId);
        if (user != null) {
            Point point = Point.builder()
                    .pointId(POINT_ID_GENERATOR.getAndIncrement())
                    .user(user)
                    .pointType(PointType.CHARGE)
                    .pointAmount(amount)
                    .createdAt(LocalDateTime.now().minusDays(7))
                    .build();

            save(point);
        }
    }

    public List<Point> findAllByUserId(Integer userId) {
        return POINT_HISTORY.getOrDefault(userId, Collections.emptyList());
    }

    public Point save(Point point) {
        // pointId가 없으면 자동 생성
        if (point.getPointId() == null) {
            point = Point.builder()
                    .pointId(POINT_ID_GENERATOR.getAndIncrement())
                    .user(point.getUser())
                    .pointType(point.getPointType())
                    .pointAmount(point.getPointAmount())
                    .createdAt(point.getCreatedAt())
                    .build();
        }

        List<Point> history = POINT_HISTORY.computeIfAbsent(
                point.getUser().getUserId(),
                k -> new ArrayList<>()
        );
        history.add(point);

        return point;
    }

    public void delete(Integer pointId) {
        POINT_HISTORY.remove(pointId);
    }
}