package com.example.ecommerceapi.cart.domain.entity;

import com.example.ecommerceapi.common.exception.CartException;
import com.example.ecommerceapi.common.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {
    private Integer cartItemId;
    private Integer userId;
    private Integer productId;
    private String productName;
    private Integer productPrice;
    private Integer quantity;
    private Integer totalPrice;
    private LocalDateTime createdAt;

    public static CartItem createAddCartItem(Integer userId,
                                             Integer productId,
                                             String productName,
                                             Integer productPrice,
                                             Integer quantity) {
        validateQuantity(quantity);
        CartItem item = new CartItem();
        item.userId = userId;
        item.productId = productId;
        item.productName = productName;
        item.productPrice = productPrice;
        item.changeQuantityAndPrice(quantity); // 내부에서 totalPrice 계산
        item.createdAt = LocalDateTime.now();
        return item;
    }

    public void changeQuantityAndPrice(Integer newQuantity) {
        validateQuantity(newQuantity);
        this.quantity = newQuantity;
        this.totalPrice = this.productPrice * this.quantity;
    }

    private static void validateQuantity(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new CartException(ErrorCode.CART_INVALID_QUANTITY);
        }
    }

}