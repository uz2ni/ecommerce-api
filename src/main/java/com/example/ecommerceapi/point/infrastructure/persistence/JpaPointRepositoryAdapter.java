package com.example.ecommerceapi.point.infrastructure.persistence;

import com.example.ecommerceapi.point.domain.entity.Point;
import com.example.ecommerceapi.point.domain.entity.PointType;
import com.example.ecommerceapi.point.domain.repository.PointRepository;
import com.example.ecommerceapi.user.domain.entity.User;
import com.example.ecommerceapi.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * PointRepository의 JPA 구현체
 * JpaPointRepository를 사용하여 실제 DB 연동
 */
@Repository
@Slf4j
@Primary
@RequiredArgsConstructor
public class JpaPointRepositoryAdapter implements PointRepository {

    private final JpaPointRepository jpaPointRepository;
    private final UserRepository userRepository;

    @Override
    public List<Point> findAllByUserId(Integer userId) {
        return jpaPointRepository.findAllByUser_UserIdOrderByCreatedAtAsc(userId);
    }

    @Override
    public Point save(Point point) {
        return jpaPointRepository.save(point);
    }

    @Override
    public void delete(Integer pointId) {
        jpaPointRepository.deleteById(pointId);
    }

    @Override
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
                    .user(user)
                    .pointType(PointType.CHARGE)
                    .pointAmount(amount)
                    .createdAt(LocalDateTime.now().minusDays(7))
                    .build();

            save(point);
        }
    }
}
