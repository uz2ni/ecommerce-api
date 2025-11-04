package com.example.ecommerceapi.common.exception;

public class OrderException extends BusinessException {

    protected OrderException(ErrorCode errorCode) {
        super(errorCode);
    }

}