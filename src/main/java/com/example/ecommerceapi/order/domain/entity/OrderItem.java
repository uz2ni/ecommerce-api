package com.example.ecommerceapi.order.domain.entity;

import com.example.ecommerceapi.product.domain.entity.Product;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "order_item", indexes = {
    @Index(name = "idx_order_item_order_id", columnList = "order_id"),
    @Index(name = "idx_order_item_product_id", columnList = "product_id")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_item_id")
    private Integer orderItemId;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Order order;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Product product;

    @Column(name = "product_name", nullable = false, length = 100)
    private String productName;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(name = "product_price", nullable = false)
    private Integer productPrice;

    @Column(name = "order_quantity", nullable = false)
    private Integer orderQuantity;

    @Column(name = "total_price", nullable = false)
    private Integer totalPrice;

    /**
     * 주문 상품을 생성합니다.
     */
    public static OrderItem createOrderItem(Order order,
                                             Product product,
                                             Integer orderQuantity) {
        return OrderItem.builder()
                .order(order)
                .product(product)
                .productName(product.getProductName())
                .description(product.getDescription())
                .productPrice(product.getProductPrice())
                .orderQuantity(orderQuantity)
                .totalPrice(product.getProductPrice() * orderQuantity)
                .build();
    }
}