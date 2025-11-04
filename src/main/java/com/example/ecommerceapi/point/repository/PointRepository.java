package com.example.ecommerceapi.point.repository;

import com.example.ecommerceapi.point.entity.Point;

import java.util.List;

public interface PointRepository {

    List<Point> findAllByUserId(Integer userId);

    Point save(Point point);
}
