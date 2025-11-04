package com.example.ecommerceapi.point.mapper;

import com.example.ecommerceapi.point.dto.PointResponse;
import com.example.ecommerceapi.point.entity.Point;
import com.example.ecommerceapi.point.entity.PointType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PointMapper {

    @Mapping(target = "pointType", expression = "java(pointTypeToString(point.getPointType()))")
    PointResponse toResponse(Point point);

    @Mapping(target = "pointType", expression = "java(stringToPointType(dto.getPointType()))")
    Point toDomain(PointResponse dto);

    /**
     * PointType enum을 String으로 변환합니다.
     * @param pointType PointType enum
     * @return enum의 name 값 (CHARGE, USE, REFUND)
     */
    default String pointTypeToString(PointType pointType) {
        return pointType != null ? pointType.name() : null;
    }

    /**
     * String을 PointType enum으로 변환합니다.
     * @param pointType String 값 (CHARGE, USE, REFUND)
     * @return PointType enum
     */
    default PointType stringToPointType(String pointType) {
        return pointType != null ? PointType.valueOf(pointType) : null;
    }
}