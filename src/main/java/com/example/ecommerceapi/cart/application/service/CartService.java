package com.example.ecommerceapi.cart.application.service;

import com.example.ecommerceapi.cart.application.dto.AddCartItemCommand;
import com.example.ecommerceapi.cart.application.dto.CartItemResult;
import com.example.ecommerceapi.cart.domain.entity.CartItem;
import com.example.ecommerceapi.cart.domain.repository.CartItemRepository;
import com.example.ecommerceapi.common.exception.CartException;
import com.example.ecommerceapi.common.exception.ErrorCode;
import com.example.ecommerceapi.product.application.validator.ProductValidator;
import com.example.ecommerceapi.product.domain.entity.Product;
import com.example.ecommerceapi.product.domain.repository.ProductRepository;
import com.example.ecommerceapi.user.application.validator.UserValidator;
import com.example.ecommerceapi.user.domain.entity.User;
import com.example.ecommerceapi.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartService {

    private final UserValidator userValidator;
    private final ProductValidator productValidator;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public List<CartItemResult> getCartItems(Integer userId) {
        // 1. 회원 존재 검증
        userValidator.validateAndGetUser(userId);

        List<CartItem> cartItems = cartItemRepository.findByUserId(userId);

        return CartItemResult.fromList(cartItems);
    }

    @Transactional
    public CartItemResult addCartItem(AddCartItemCommand command) {
        // 1. 회원 존재 검증
        User user = userValidator.validateAndGetUser(command.userId());

        // 2. 상품 존재 검증
        Product product = productValidator.validateAndGetProduct(command.productId());

        // 3. 재고 검증
        product.validateStock(command.quantity());

        // 4. 이미 담긴 상품인지 확인
        Optional<CartItem> existingItem = cartItemRepository.findByUserIdAndProductId(command.userId(), command.productId());
        CartItem cartItem;
        if (existingItem.isPresent()) {
            // 정책: 수량,금액 덮어쓰기
            cartItem = existingItem.get();
            cartItem.changeQuantityAndPrice(command.quantity());
            cartItem = cartItemRepository.save(cartItem);
        } else {
            // 신규 생성
            cartItem = CartItem.createAddCartItem(
                    user,
                    product,
                    command.quantity()
            );
            cartItem = cartItemRepository.save(cartItem);
        }

        return CartItemResult.from(cartItem);
    }

    @Transactional
    public Integer deleteCartItem(Integer cartItemId) {
        Optional<CartItem> existingItem = cartItemRepository.findById(cartItemId);
        if (existingItem.isEmpty()) {
            throw new CartException(ErrorCode.CART_ITEM_NOT_FOUND);
        }
        cartItemRepository.deleteById(cartItemId);
        return cartItemId;
    }

    /**
     * 초기 장바구니 데이터 생성 (테스트/개발용)
     */
    @Transactional
    public void init() {
        log.info("Initializing cart items...");

        User user1 = userRepository.findById(1);
        User user2 = userRepository.findById(2);

        Product product1 = productRepository.findById(1);
        Product product2 = productRepository.findById(2);
        Product product3 = productRepository.findById(3);

        // 사용자 1의 장바구니 - 유기농 딸기 2개
        CartItem item1 = CartItem.builder()
                .user(user1)
                .product(product1)
                .productName(product1.getProductName())
                .productPrice(product1.getProductPrice())
                .quantity(2)
                .totalPrice(product1.getProductPrice() * 2)
                .createdAt(LocalDateTime.now().minusDays(1))
                .build();
        cartItemRepository.save(item1);

        // 사용자 1의 장바구니 - 제주 감귤 1개
        CartItem item2 = CartItem.builder()
                .user(user1)
                .product(product2)
                .productName(product2.getProductName())
                .productPrice(product2.getProductPrice())
                .quantity(1)
                .totalPrice(product2.getProductPrice())
                .createdAt(LocalDateTime.now().minusHours(5))
                .build();
        cartItemRepository.save(item2);

        // 사용자 2의 장바구니 - 티라미수 케이크 1개
        CartItem item3 = CartItem.builder()
                .user(user2)
                .product(product3)
                .productName(product3.getProductName())
                .productPrice(product3.getProductPrice())
                .quantity(1)
                .totalPrice(product3.getProductPrice())
                .createdAt(LocalDateTime.now().minusHours(2))
                .build();
        cartItemRepository.save(item3);

        log.info("Cart items initialization completed");
    }
}