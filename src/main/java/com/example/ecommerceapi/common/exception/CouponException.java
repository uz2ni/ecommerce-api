package com.example.ecommerceapi.common.exception;

public class CouponException extends BusinessException {

    public CouponException(ErrorCode errorCode) {
        super(errorCode);
    }

}