package com.example.ecommerceapi.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    /**
     * 상품 (PD__)
     */
    // PRODUCT_NOT_FOUND("PD01", "존재하는 상품이 아닙니다.");

    /**
     * 주문/결제(OD__)
     */

    /**
     * 장바구니(CT__)
     */

    /**
     * 쿠폰(CP__)
     */

    /**
     * 회원(US__)
     */

    /**
     * 서버, 그 외
     */
    FIELD_NOT_VALID("FD0001", "유효하지 않은 필드 값입니다."),
    SERVER_ERROR("SV0001", "서버 에러가 발생했습니다.");

    private final String code;
    private final String message;
}