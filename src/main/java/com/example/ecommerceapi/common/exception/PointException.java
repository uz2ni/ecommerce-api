package com.example.ecommerceapi.common.exception;

public class PointException extends BusinessException {

    public PointException(ErrorCode errorCode) {
        super(errorCode);
    }
}