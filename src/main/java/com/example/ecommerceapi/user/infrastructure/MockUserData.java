package com.example.ecommerceapi.user.infrastructure;

import com.example.ecommerceapi.user.presentation.dto.UserResponse;

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
                .pointBalance(500000)
                .build());

        USERS.put(2, UserResponse.builder()
                .userId(2)
                .username("이영희")
                .pointBalance(1000000)
                .build());

        USERS.put(3, UserResponse.builder()
                .userId(3)
                .username("박민수")
                .pointBalance(300000)
                .build());

        USERS.put(4, UserResponse.builder()
                .userId(4)
                .username("정수진")
                .pointBalance(750000)
                .build());

        USERS.put(5, UserResponse.builder()
                .userId(5)
                .username("최동욱")
                .pointBalance(2000000)
                .build());
    }

    public static List<UserResponse> getAllUsers() {
        return new ArrayList<>(USERS.values());
    }

    public static UserResponse getUser(Integer userId) {
        return USERS.get(userId);
    }

    public static Integer getBalance(Integer userId) {
        UserResponse user = USERS.get(userId);
        return user != null ? user.getPointBalance() : 0;
    }

    public static void updateBalance(Integer userId, Integer newBalance) {
        UserResponse user = USERS.get(userId);
        if (user != null) {
            user.setPointBalance(newBalance);
        }
    }
}