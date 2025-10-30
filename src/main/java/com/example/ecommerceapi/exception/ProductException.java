package com.example.ecommerceapi.exception;

public class ProductException extends BusinessException {

    protected ProductException(ErrorCode errorCode) {
        super(errorCode);
    }

}