package com.example.ecommerceapi.exception;

public class CouponException extends BusinessException {

    protected CouponException(ErrorCode errorCode) {
        super(errorCode);
    }

}