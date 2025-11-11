package com.example.ecommerceapi.user.infrastructure.persistence;

import com.example.ecommerceapi.user.domain.entity.User;
import com.example.ecommerceapi.user.domain.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * UserRepository의 JPA 구현체
 * JpaUserRepository를 사용하여 실제 DB 연동
 */
@Repository
@Slf4j
@Primary
@RequiredArgsConstructor
public class JpaUserRepositoryAdapter implements UserRepository {

    private final JpaUserRepository jpaUserRepository;

    @Override
    public List<User> findAll() {
        return jpaUserRepository.findAll();
    }

    @Override
    public User findById(Integer userId) {
        return jpaUserRepository.findById(userId).orElse(null);
    }

    @Override
    public Integer findBalanceById(Integer userId) {
        return jpaUserRepository.findBalanceByUserId(userId).orElse(null);
    }

    @Override
    public void save(User user) {
        jpaUserRepository.save(user);
    }

    @Override
    public void init() {
        jpaUserRepository.save(User.builder()
                .username("김철수")
                .pointBalance(500000)
                .build());

        jpaUserRepository.save(User.builder()
                .username("이영희")
                .pointBalance(1000000)
                .build());

        jpaUserRepository.save(User.builder()
                .username("박민수")
                .pointBalance(300000)
                .build());

        jpaUserRepository.save(User.builder()
                .username("정수진")
                .pointBalance(750000)
                .build());

        jpaUserRepository.save(User.builder()
                .username("최동욱")
                .pointBalance(2000000)
                .build());
    }
}
