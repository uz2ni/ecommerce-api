package com.example.ecommerceapi.common.exception;

public class CartException extends BusinessException {

    protected CartException(ErrorCode errorCode) {
        super(errorCode);
    }

}