package com.example.ecommerceapi.common.exception;

public class ProductException extends BusinessException {

    protected ProductException(ErrorCode errorCode) {
        super(errorCode);
    }

}