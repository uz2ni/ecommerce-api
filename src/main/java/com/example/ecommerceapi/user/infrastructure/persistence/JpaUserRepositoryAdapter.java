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
@Slf4j
@Repository
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

    /**
     * 동시성 제어가 필요한 경우 사용하는 조회 메서드
     * 비관적 락(PESSIMISTIC_WRITE)을 적용하여 SELECT FOR UPDATE 실행
     */
    @Override
    public User findByIdWithLock(Integer userId) {
        return jpaUserRepository.findByIdWithLock(userId).orElse(null);
    }

    @Override
    public Integer findBalanceById(Integer userId) {
        return jpaUserRepository.findBalanceByUserId(userId).orElse(null);
    }

    @Override
    public User save(User user) {
        return jpaUserRepository.save(user);
    }

    @Override
    public void init() {
    }
}
