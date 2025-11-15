package com.example.ecommerceapi.cart.infrastructure.persistence;

import com.example.ecommerceapi.cart.domain.entity.CartItem;
import com.example.ecommerceapi.cart.domain.repository.CartItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


/**
 * CartItemRepository의 JPA 구현체
 * JpaCartItemRepository를 사용하여 실제 DB 연동
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class JpaCartItemRepositoryAdapter implements CartItemRepository {

    private final JpaCartItemRepository jpaCartItemRepository;
    private final CartItemTableUtils cartItemTableUtils;

    @Override
    public CartItem save(CartItem cartItem) {
        return jpaCartItemRepository.save(cartItem);
    }

    @Override
    public Optional<CartItem> findById(Integer cartItemId) {
        return jpaCartItemRepository.findById(cartItemId);
    }

    @Override
    public List<CartItem> findByUserId(Integer userId) {
        return jpaCartItemRepository.findByUserUserId(userId);
    }

    @Override
    public Optional<CartItem> findByUserIdAndProductId(Integer userId, Integer productId) {
        return jpaCartItemRepository.findByUserUserIdAndProductProductId(userId, productId);
    }

    @Override
    public boolean deleteById(Integer cartItemId) {
        if (jpaCartItemRepository.existsById(cartItemId)) {
            jpaCartItemRepository.deleteById(cartItemId);
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public void deleteByUserIdAndProductIds(Integer userId, List<Integer> productIds) {
        jpaCartItemRepository.deleteByUserUserIdAndProductProductIdIn(userId, productIds);
    }

    @Override
    @Transactional
    public void deleteByUserId(Integer userId) {
        jpaCartItemRepository.deleteByUserUserId(userId);
    }

    @Override
    public void clear() {
        cartItemTableUtils.resetCartItemTable();
    }

    @Override
    public int count() {
        return (int) jpaCartItemRepository.count();
    }

    @Override
    public void init() {
        // DataInitializer에서 초기화 수행
    }
}
