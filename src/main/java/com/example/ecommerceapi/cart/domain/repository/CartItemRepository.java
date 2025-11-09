package com.example.ecommerceapi.cart.domain.repository;

import com.example.ecommerceapi.cart.domain.entity.CartItem;

import java.util.List;
import java.util.Optional;

/**
 * CartItem 도메인의 Repository 인터페이스
 * 구현체: InMemoryCartItemRepository (추후 JpaCartItemRepository 등으로 확장 가능)
 */
public interface CartItemRepository {

    /**
     * 장바구니 아이템 저장
     */
    CartItem save(CartItem cartItem);

    /**
     * ID로 장바구니 아이템 조회
     */
    Optional<CartItem> findById(Integer cartItemId);

    /**
     * 사용자 ID로 장바구니 아이템 목록 조회
     */
    List<CartItem> findByUserId(Integer userId);

    /**
     * 사용자 ID와 상품 ID로 장바구니 아이템 조회
     */
    Optional<CartItem> findByUserIdAndProductId(Integer userId, Integer productId);

    /**
     * ID로 장바구니 아이템 삭제
     */
    boolean deleteById(Integer cartItemId);

    /**
     * 사용자 ID와 상품 ID 목록으로 장바구니 아이템 삭제
     */
    void deleteByUserIdAndProductIds(Integer userId, List<Integer> productIds);

    /**
     * 사용자 ID로 장바구니 아이템 전체 삭제
     */
    void deleteByUserId(Integer userId);

    /**
     * 모든 장바구니 아이템 삭제 (테스트용)
     */
    void clear();

    /**
     * 장바구니 아이템 개수 조회
     */
    int count();

    /**
     * 초기 장바구니 데이터 생성 (테스트용)
     */
    void init();
}