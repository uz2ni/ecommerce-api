package com.example.ecommerceapi.cart.application.service;

import com.example.ecommerceapi.cart.application.dto.AddCartItemCommand;
import com.example.ecommerceapi.cart.application.dto.CartItemResult;
import com.example.ecommerceapi.cart.domain.entity.CartItem;
import com.example.ecommerceapi.cart.domain.repository.CartItemRepository;
import com.example.ecommerceapi.common.exception.CartException;
import com.example.ecommerceapi.common.exception.ErrorCode;
import com.example.ecommerceapi.product.application.validator.ProductValidator;
import com.example.ecommerceapi.product.domain.entity.Product;
import com.example.ecommerceapi.user.application.validator.UserValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Primary
@RequiredArgsConstructor
public class CartService {

    private final UserValidator userValidator;
    private final ProductValidator productValidator;
    private final CartItemRepository cartItemRepository;

    public List<CartItemResult> getCartItems(Integer userId) {
        // 1. 회원 존재 검증
        userValidator.validateAndGetUser(userId);

        List<CartItem> cartItems = cartItemRepository.findByUserId(userId);

        return CartItemResult.fromList(cartItems);
    }

    public CartItemResult addCartItem(AddCartItemCommand command) {
        // 1. 회원 존재 검증
        userValidator.validateAndGetUser(command.getUserId());

        // 2. 상품 존재 검증
        Product product = productValidator.validateAndGetProduct(command.getProductId());

        // 3. 재고 검증
        if (command.getQuantity() > product.getQuantity()) {
            throw new CartException(ErrorCode.CART_EXCEED_STOCK);
        }

        // 4. 이미 담긴 상품인지 확인
        Optional<CartItem> existingItem = cartItemRepository.findByUserIdAndProductId(command.getUserId(), command.getProductId());
        CartItem cartItem;
        if (existingItem.isPresent()) {
            // 정책: 수량,금액 덮어쓰기
            cartItem = existingItem.get();
            cartItem.changeQuantityAndPrice(command.getQuantity());
            cartItem = cartItemRepository.save(cartItem);
        } else {
            // 신규 생성
            cartItem = CartItem.createAddCartItem(
                    command.getUserId(),
                    command.getProductId(),
                    product.getProductName(),
                    product.getProductPrice(),
                    command.getQuantity()
            );
            cartItem = cartItemRepository.save(cartItem);
        }

        return CartItemResult.from(cartItem);
    }

    public Integer deleteCartItem(Integer cartItemId) {
        Optional<CartItem> existingItem = cartItemRepository.findById(cartItemId);
        if (existingItem.isEmpty()) {
            throw new CartException(ErrorCode.CART_ITEM_NOT_FOUND);
        }
        cartItemRepository.deleteById(cartItemId);
        return cartItemId;
    }
}