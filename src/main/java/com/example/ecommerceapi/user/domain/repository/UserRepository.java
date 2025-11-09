package com.example.ecommerceapi.user.domain.repository;

import com.example.ecommerceapi.user.domain.entity.User;

import java.util.List;

/**
 * User 도메인의 Repository 인터페이스
 * 구현체: InMemoryUserRepository (추후 JpaUserRepository 등으로 확장 가능)
 */
public interface UserRepository {

    /**
     * 모든 사용자 조회
     */
    List<User> findAll();

    /**
     * ID로 사용자 조회
     */
    User findById(Integer userId);

    /**
     * ID로 사용자의 포인트 잔액 조회
     */
    Integer findBalanceById(Integer userId);

    /**
     * 사용자의 포인트 잔액 업데이트
     */
    void updateBalance(Integer userId, Integer newBalance);

    /**
     * 사용자 저장 (생성/수정)
     */
    void save(User user);

    /**
     * 모든 사용자 삭제 (테스트용)
     */
    void clear();

    /**
     * 초기 사용자 데이터 생성 (테스트용)
     */
    void init();
}