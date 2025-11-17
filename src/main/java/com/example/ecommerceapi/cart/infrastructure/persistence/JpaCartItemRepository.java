package com.example.ecommerceapi.cart.infrastructure.persistence;

import com.example.ecommerceapi.cart.domain.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JpaCartItemRepository extends JpaRepository<CartItem, Integer> {

    /**
     * 사용자 ID로 장바구니 아이템 목록 조회
     */
    List<CartItem> findByUserUserId(Integer userId);

    /**
     * 사용자 ID와 상품 ID로 장바구니 아이템 조회
     */
    Optional<CartItem> findByUserUserIdAndProductProductId(Integer userId, Integer productId);

    /**
     * 사용자 ID와 상품 ID 목록으로 장바구니 아이템 삭제
     */
    void deleteByUserUserIdAndProductProductIdIn(Integer userId, List<Integer> productIds);

    /**
     * 사용자 ID로 장바구니 아이템 전체 삭제
     */
    void deleteByUserUserId(Integer userId);
}
