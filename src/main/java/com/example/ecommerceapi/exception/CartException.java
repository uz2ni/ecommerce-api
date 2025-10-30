package com.example.ecommerceapi.exception;

public class CartException extends BusinessException {

    protected CartException(ErrorCode errorCode) {
        super(errorCode);
    }

}