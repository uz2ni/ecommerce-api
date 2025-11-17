package com.example.ecommerceapi.user.infrastructure.persistence;

import com.example.ecommerceapi.user.domain.entity.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * User 도메인의 JPA Repository
 */
public interface JpaUserRepository extends JpaRepository<User, Integer> {

    /**
     * ID로 사용자 조회 (비관적 락 적용)
     * PESSIMISTIC_WRITE 사용 시, 동시성 제어를 위해 SELECT FOR UPDATE 사용됨
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM User u WHERE u.userId = :userId")
    Optional<User> findByIdWithLock(@Param("userId") Integer userId);

    /**
     * ID로 사용자의 포인트 잔액만 조회
     */
    @Query("SELECT u.pointBalance FROM User u WHERE u.userId = :userId")
    Optional<Integer> findBalanceByUserId(@Param("userId") Integer userId);
}
