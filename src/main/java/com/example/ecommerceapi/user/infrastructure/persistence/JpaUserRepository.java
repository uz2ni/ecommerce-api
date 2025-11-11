package com.example.ecommerceapi.user.infrastructure.persistence;

import com.example.ecommerceapi.user.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * User 도메인의 JPA Repository
 */
public interface JpaUserRepository extends JpaRepository<User, Integer> {

    /**
     * ID로 사용자의 포인트 잔액만 조회
     */
    @Query("SELECT u.pointBalance FROM User u WHERE u.userId = :userId")
    Optional<Integer> findBalanceByUserId(@Param("userId") Integer userId);
}
