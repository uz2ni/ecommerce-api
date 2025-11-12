package com.example.ecommerceapi.cart.domain.entity;

import com.example.ecommerceapi.common.exception.CartException;
import com.example.ecommerceapi.common.exception.ErrorCode;
import com.example.ecommerceapi.product.domain.entity.Product;
import com.example.ecommerceapi.user.domain.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "cart_item", indexes = {
    @Index(name = "idx_cart_item_user_id", columnList = "user_id"),
    @Index(name = "idx_cart_item_product_id", columnList = "product_id")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_item_id")
    private Integer cartItemId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private User user;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Product product;

    @Column(name = "product_name", nullable = false, length = 100)
    private String productName;

    @Column(name = "product_price", nullable = false)
    private Integer productPrice;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "total_price", nullable = false)
    private Integer totalPrice;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public static CartItem createAddCartItem(User user,
                                             Product product,
                                             Integer quantity) {
        validateQuantity(quantity);
        CartItem item = new CartItem();
        item.user = user;
        item.product = product;
        item.productName = product.getProductName();
        item.productPrice = product.getProductPrice();
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

    public CartItem deepCopy() {
        return CartItem.builder()
                .cartItemId(this.cartItemId)   // 복원 시 다시 넣을 수 있도록
                .user(this.user)
                .product(this.product)
                .productName(this.productName)
                .productPrice(this.productPrice)
                .quantity(this.quantity)
                .totalPrice(this.totalPrice)
                .createdAt(this.createdAt)
                .build();
    }

    // Helper methods for backward compatibility
    public Integer getUserId() {
        return user != null ? user.getUserId() : null;
    }

    public Integer getProductId() {
        return product != null ? product.getProductId() : null;
    }
}