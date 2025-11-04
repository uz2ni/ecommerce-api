package com.example.ecommerceapi.application.mapper;

import com.example.ecommerceapi.application.dto.user.UserPointBalanceResponse;
import com.example.ecommerceapi.application.dto.user.UserResponse;
import com.example.ecommerceapi.domain.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring") // Spring Bean으로 등록. 생성자 주입 가능
public interface UserMapper {

    UserResponse toResponse(User user);

    User toDomain(UserResponse dto);

    UserPointBalanceResponse toUserPointBalanceResponse(Integer userId, Integer pointBalance);
}