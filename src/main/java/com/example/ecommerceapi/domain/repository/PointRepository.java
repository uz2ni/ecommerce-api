package com.example.ecommerceapi.domain.repository;

import com.example.ecommerceapi.domain.entity.Point;

import java.util.List;

public interface PointRepository {

    List<Point> findAllByUserId(Integer userId);

    Point save(Point point);
}
