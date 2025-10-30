package com.example.ecommerceapi.exception;

import lombok.Getter;

@Getter
public abstract class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    protected BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public String getErrorCodeValue() {
        return errorCode.getCode();
    }

    public String getErrorCodeMessage() {
        return errorCode.getMessage();
    }

}