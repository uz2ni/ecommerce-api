package com.example.ecommerceapi.point.domain.repository;

import com.example.ecommerceapi.point.domain.entity.Point;

import java.util.List;

/**
 * Point 도메인의 Repository 인터페이스
 * 구현체: InMemoryPointRepository (추후 JpaPointRepository 등으로 확장 가능)
 */
public interface PointRepository {

    /**
     * 사용자 ID로 포인트 이력 조회
     */
    List<Point> findAllByUserId(Integer userId);

    /**
     * 포인트 이력 저장
     */
    Point save(Point point);

    /**
     * 포인트 이력 삭제
     */
    void delete(Integer pointId);

    /**
     * 초기 포인트 이력 데이터 생성 (테스트용)
     */
    void init();
}