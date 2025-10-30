package com.example.ecommerceapi.exception;

public class UserException extends BusinessException {

    protected UserException(ErrorCode errorCode) {
        super(errorCode);
    }

}