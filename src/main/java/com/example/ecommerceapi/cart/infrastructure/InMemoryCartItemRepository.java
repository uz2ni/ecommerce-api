package com.example.ecommerceapi.cart.infrastructure;

import com.example.ecommerceapi.cart.domain.entity.CartItem;
import jakarta.annotation.PostConstruct;
import com.example.ecommerceapi.cart.domain.repository.CartItemRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Repository
public class InMemoryCartItemRepository implements CartItemRepository {

    private final Map<Integer, CartItem> store = new ConcurrentHashMap<>();
    private final AtomicInteger idGenerator = new AtomicInteger(1);

    @PostConstruct
    public void init() {
        // 사용자 1의 장바구니 - 유기농 딸기 2개
        CartItem item1 = CartItem.builder()
                .cartItemId(idGenerator.getAndIncrement())
                .userId(1)
                .productId(1)
                .productName("유기농 딸기")
                .productPrice(18900)
                .quantity(2)
                .totalPrice(37800)
                .createdAt(LocalDateTime.now().minusDays(1))
                .build();
        store.put(item1.getCartItemId(), item1);

        // 사용자 1의 장바구니 - 제주 감귤 1개
        CartItem item2 = CartItem.builder()
                .cartItemId(idGenerator.getAndIncrement())
                .userId(1)
                .productId(2)
                .productName("제주 감귤")
                .productPrice(12900)
                .quantity(1)
                .totalPrice(12900)
                .createdAt(LocalDateTime.now().minusHours(5))
                .build();
        store.put(item2.getCartItemId(), item2);

        // 사용자 2의 장바구니 - 티라미수 케이크 1개
        CartItem item3 = CartItem.builder()
                .cartItemId(idGenerator.getAndIncrement())
                .userId(2)
                .productId(3)
                .productName("티라미수 케이크")
                .productPrice(32000)
                .quantity(1)
                .totalPrice(32000)
                .createdAt(LocalDateTime.now().minusHours(2))
                .build();
        store.put(item3.getCartItemId(), item3);
    }

    /**
     * 장바구니 아이템 저장
     * cartItemId가 없으면 새로운 ID 부여, 있으면 업데이트
     */
    public CartItem save(CartItem cartItem) {
        if (cartItem.getCartItemId() == null) {
            cartItem.setCartItemId(idGenerator.getAndIncrement());
        }
        store.put(cartItem.getCartItemId(), cartItem);
        return cartItem;
    }

    /**
     * ID로 장바구니 아이템 조회
     */
    public Optional<CartItem> findById(Integer cartItemId) {
        return Optional.ofNullable(store.get(cartItemId));
    }

    /**
     * 사용자의 모든 장바구니 아이템 조회
     */
    public List<CartItem> findByUserId(Integer userId) {
        return store.values().stream()
                .filter(item -> item.getUserId().equals(userId))
                .collect(Collectors.toList());
    }

    /**
     * 사용자의 특정 상품이 장바구니에 있는지 조회
     */
    public Optional<CartItem> findByUserIdAndProductId(Integer userId, Integer productId) {
        return store.values().stream()
                .filter(item -> item.getUserId().equals(userId)
                        && item.getProductId().equals(productId))
                .findFirst();
    }

    /**
     * 장바구니 아이템 삭제
     */
    public boolean deleteById(Integer cartItemId) {
        return store.remove(cartItemId) != null;
    }

    /**
     * 사용자의 특정 상품들을 장바구니에서 삭제
     */
    public void deleteByUserIdAndProductIds(Integer userId, List<Integer> productIds) {
        List<Integer> itemsToDelete = store.values().stream()
                .filter(item -> item.getUserId().equals(userId)
                        && productIds.contains(item.getProductId()))
                .map(CartItem::getCartItemId)
                .collect(Collectors.toList());

        itemsToDelete.forEach(store::remove);
    }

    /**
     * 모든 데이터 삭제 (테스트용)
     */
    public void clear() {
        store.clear();
        idGenerator.set(1);
    }

    /**
     * 전체 장바구니 아이템 개수 조회 (테스트용)
     */
    public int count() {
        return store.size();
    }

    /**
     * 회원 전체 장바구니 아이템 삭제
     */
    public void deleteByUserId(Integer userId) {
        store.entrySet().removeIf(entry ->
                entry.getValue().getUserId().equals(userId)
        );
    }
}
