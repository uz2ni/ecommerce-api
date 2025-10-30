package com.example.ecommerceapi.exception;

public class OrderException extends BusinessException {

    protected OrderException(ErrorCode errorCode) {
        super(errorCode);
    }

}