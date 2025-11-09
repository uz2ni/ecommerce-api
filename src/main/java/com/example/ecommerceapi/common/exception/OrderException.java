package com.example.ecommerceapi.common.exception;

public class OrderException extends BusinessException {

    public OrderException(ErrorCode errorCode) {
        super(errorCode);
    }

}