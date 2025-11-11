package com.example.ecommerceapi.product.domain.entity;

import com.example.ecommerceapi.common.exception.ErrorCode;
import com.example.ecommerceapi.common.exception.ProductException;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "product", indexes = {
    @Index(name = "idx_product_view_count", columnList = "view_count")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Integer productId;

    @Column(name = "product_name", nullable = false, length = 100)
    private String productName;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(name = "product_price", nullable = false)
    private Integer productPrice;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "view_count", nullable = false)
    private Integer viewCount;

    @Version
    @Column(nullable = false)
    private Integer version;

    public void incrementViewCount() {
        this.viewCount += 1;
    }

    /**
     * 재고가 충분한지 검증합니다.
     * @param requestedQuantity 요청 수량
     * @throws ProductException 재고가 부족한 경우
     */
    public void validateStock(Integer requestedQuantity) {
        if (this.quantity < requestedQuantity) {
            throw new ProductException(ErrorCode.PRODUCT_STOCK_INSUFFICIENT);
        }
    }

    /**
     * 재고를 차감합니다.
     * @param quantity 차감할 수량
     * @return 차감 후 남은 재고 수량
     */
    public Integer decreaseStock(Integer quantity) {
        validateStock(quantity);
        this.quantity -= quantity;
        return this.quantity;
    }

    /**
     * 재고를 증가합니다.
     * @param quantity 증가할 수량
     * @return 증가 후 남은 재고 수량
     */
    public void increaseStock(Integer quantity) {
        if (quantity <= 0) {
            throw new ProductException(ErrorCode.PRODUCT_INVALID_STOCK_INCREMENT);
        }
        this.quantity += quantity;
    }
}
