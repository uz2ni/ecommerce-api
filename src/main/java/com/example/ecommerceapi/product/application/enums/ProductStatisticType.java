package com.example.ecommerceapi.product.application.enums;

import com.example.ecommerceapi.common.exception.ErrorCode;
import com.example.ecommerceapi.common.exception.ProductException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProductStatisticType {
    SALES,
    VIEWS;

    public static ProductStatisticType from(String type) {
        try {
            return ProductStatisticType.valueOf(type);
        } catch (IllegalArgumentException e) {
            throw new ProductException(ErrorCode.PRODUCT_NOT_VALID_STATISTIC);
        }
    }
}