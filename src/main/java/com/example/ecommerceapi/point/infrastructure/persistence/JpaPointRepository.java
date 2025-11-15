package com.example.ecommerceapi.point.infrastructure.persistence;

import com.example.ecommerceapi.point.domain.entity.Point;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Point 도메인의 JPA Repository
 */
public interface JpaPointRepository extends JpaRepository<Point, Integer> {

    /**
     * 사용자 ID로 포인트 이력 조회 (생성일시 오름차순)
     * User 엔티티와 ManyToOne 관계에서 user.userId로 조회
     */
    List<Point> findAllByUser_UserIdOrderByCreatedAtAsc(Integer userId);
}