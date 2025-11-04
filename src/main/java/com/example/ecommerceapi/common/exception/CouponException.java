package com.example.ecommerceapi.common.exception;

public class CouponException extends BusinessException {

    protected CouponException(ErrorCode errorCode) {
        super(errorCode);
    }

}