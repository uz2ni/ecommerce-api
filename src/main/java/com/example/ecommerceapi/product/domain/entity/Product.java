package com.example.ecommerceapi.product.domain.entity;

import com.example.ecommerceapi.common.exception.ErrorCode;
import com.example.ecommerceapi.common.exception.ProductException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    private Integer productId;
    private String productName;
    private String description;
    private Integer productPrice;
    private Integer quantity;
    private Integer viewCount;
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
