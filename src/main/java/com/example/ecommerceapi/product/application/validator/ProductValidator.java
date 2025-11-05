package com.example.ecommerceapi.product.application.validator;

import com.example.ecommerceapi.common.exception.ErrorCode;
import com.example.ecommerceapi.common.exception.ProductException;
import com.example.ecommerceapi.common.exception.UserException;
import com.example.ecommerceapi.product.domain.entity.Product;
import com.example.ecommerceapi.product.infrastructure.InMemoryProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductValidator {

    private final InMemoryProductRepository productRepository;

    /**
     * 상품 존재 여부를 검증하고 Product 엔티티를 반환합니다.
     * @param productId 검증할 product ID
     * @return Product 엔티티
     * @throws ProductException 상품이 존재하지 않을 경우
     */
    public Product validateAndGetProduct(Integer productId) {
        Product product = productRepository.findById(productId);
        if(product == null) {
            throw new ProductException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        return product;
    }
}
