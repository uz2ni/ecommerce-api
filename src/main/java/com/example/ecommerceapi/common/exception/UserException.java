package com.example.ecommerceapi.common.exception;

public class UserException extends BusinessException {

    public UserException(ErrorCode errorCode) {
        super(errorCode);
    }

}