package com.example.ecommerceapi.infrastructure.memory;

import com.example.ecommerceapi.application.dto.cart.CartItemResponse;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class MockCartData {

    private static final Map<Integer, List<CartItemResponse>> USER_CARTS = new HashMap<>();
    private static final AtomicInteger CART_ITEM_ID_GENERATOR = new AtomicInteger(1);

    static {
        // 사용자 1의 장바구니
        List<CartItemResponse> user1Cart = new ArrayList<>();
        user1Cart.add(CartItemResponse.builder()
                .cartItemId(CART_ITEM_ID_GENERATOR.getAndIncrement())
                .userId(1)
                .productId(1)
                .productName("유기농 딸기")
                .productPrice(18900)
                .quantity(2)
                .totalPrice(37800)
                .createdAt(LocalDateTime.now().minusDays(1))
                .build());
        user1Cart.add(CartItemResponse.builder()
                .cartItemId(CART_ITEM_ID_GENERATOR.getAndIncrement())
                .userId(1)
                .productId(2)
                .productName("제주 감귤")
                .productPrice(12900)
                .quantity(1)
                .totalPrice(12900)
                .createdAt(LocalDateTime.now().minusHours(5))
                .build());
        USER_CARTS.put(1, user1Cart);

        // 사용자 2의 장바구니
        List<CartItemResponse> user2Cart = new ArrayList<>();
        user2Cart.add(CartItemResponse.builder()
                .cartItemId(CART_ITEM_ID_GENERATOR.getAndIncrement())
                .userId(2)
                .productId(3)
                .productName("티라미수 케이크")
                .productPrice(32000)
                .quantity(1)
                .totalPrice(32000)
                .createdAt(LocalDateTime.now().minusHours(2))
                .build());
        USER_CARTS.put(2, user2Cart);
    }

    public static List<CartItemResponse> getCartItems(Integer userId) {
        return USER_CARTS.getOrDefault(userId, new ArrayList<>());
    }

    public static CartItemResponse addCartItem(Integer userId, Integer productId, Integer quantity) {
        var product = MockProductData.getProduct(productId);
        if (product == null) {
            return null;
        }

        CartItemResponse newItem = CartItemResponse.builder()
                .cartItemId(CART_ITEM_ID_GENERATOR.getAndIncrement())
                .userId(userId)
                .productId(productId)
                .productName(product.getProductName())
                .productPrice(product.getProductPrice())
                .quantity(quantity)
                .totalPrice(product.getProductPrice() * quantity)
                .createdAt(LocalDateTime.now())
                .build();

        USER_CARTS.computeIfAbsent(userId, k -> new ArrayList<>()).add(newItem);
        return newItem;
    }

    public static boolean deleteCartItem(Integer cartItemId) {
        for (List<CartItemResponse> cart : USER_CARTS.values()) {
            boolean removed = cart.removeIf(item -> item.getCartItemId().equals(cartItemId));
            if (removed) {
                return true;
            }
        }
        return false;
    }

    public static void clearCart(Integer userId) {
        List<CartItemResponse> cart = USER_CARTS.get(userId);
        if (cart != null) {
            cart.clear();
        }
    }
}