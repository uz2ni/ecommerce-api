package com.example.ecommerceapi.user.infrastructure.memory;

import com.example.ecommerceapi.user.domain.entity.User;
import com.example.ecommerceapi.user.domain.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * InMemory 기반 UserRepository 구현체
 * 테스트 환경에서만 사용
 */
@Repository
public class InMemoryUserRepository implements UserRepository {

    private Map<Integer, User> USERS = new HashMap<>();

    @PostConstruct
    public void init() {
        USERS.put(1, User.builder()
                .userId(1)
                .username("김철수")
                .pointBalance(500000)
                .build());

        USERS.put(2, User.builder()
                .userId(2)
                .username("이영희")
                .pointBalance(1000000)
                .build());

        USERS.put(3, User.builder()
                .userId(3)
                .username("박민수")
                .pointBalance(300000)
                .build());

        USERS.put(4, User.builder()
                .userId(4)
                .username("정수진")
                .pointBalance(750000)
                .build());

        USERS.put(5, User.builder()
                .userId(5)
                .username("최동욱")
                .pointBalance(2000000)
                .build());
    }

    public List<User> findAll() {
        return USERS.values().stream().toList();
    }

    public User findById(Integer userId) {
        return USERS.get(userId);
    }

    public Integer findBalanceById(Integer userId) {
        User user = USERS.get(userId);
        return user != null ? user.getPointBalance() : null;
    }

    public void save(User user) {
        USERS.put(user.getUserId(), user);
    }

    public void clear() {
        USERS.clear();
    }
}
