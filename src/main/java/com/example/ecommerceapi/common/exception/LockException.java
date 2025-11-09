package com.example.ecommerceapi.common.exception;

public class LockException extends BusinessException {

    public LockException(ErrorCode errorCode) {
        super(errorCode);
    }
}