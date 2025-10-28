package com.example.ecommerceapi.mock;

import com.example.ecommerceapi.dto.user.UserResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MockUserData {

    private static final Map<Integer, UserResponse> USERS = new HashMap<>();

    static {
        USERS.put(1, UserResponse.builder()
                .userId(1)
                .username("김철수")
                .build());

        USERS.put(2, UserResponse.builder()
                .userId(2)
                .username("이영희")
                .build());

        USERS.put(3, UserResponse.builder()
                .userId(3)
                .username("박민수")
                .build());

        USERS.put(4, UserResponse.builder()
                .userId(4)
                .username("정수진")
                .build());

        USERS.put(5, UserResponse.builder()
                .userId(5)
                .username("최동욱")
                .build());
    }

    public static List<UserResponse> getAllUsers() {
        return new ArrayList<>(USERS.values());
    }

    public static UserResponse getUser(Integer userId) {
        return USERS.get(userId);
    }
}