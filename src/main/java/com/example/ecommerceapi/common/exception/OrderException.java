package com.example.ecommerceapi.common.exception;

public class OrderException extends BusinessException {

    public OrderException(ErrorCode errorCode) {
        super(errorCode);
    }

    public OrderException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }

}