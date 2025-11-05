package com.example.ecommerceapi.common.exception;

public class CartException extends BusinessException {

    public CartException(ErrorCode errorCode) {
        super(errorCode);
    }

}