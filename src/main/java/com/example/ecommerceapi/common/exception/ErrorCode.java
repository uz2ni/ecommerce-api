package com.example.ecommerceapi.common.exception;

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
    USER_NOT_FOUND("US01", "회원이 존재하지 않습니다."),

    /**
     * 포인트(PT__)
     */
    POINT_INSUFFICIENT_BALANCE("PT01", "포인트 잔액이 부족합니다."),
    POINT_INVALID_AMOUNT("PT02", "포인트 금액이 유효하지 않습니다."),
    POINT_CHARGE_IN_PROGRESS("PT03", "포인트 충전 요청이 처리 중입니다. 잠시 후 다시 시도해주세요."),

    /**
     * 동시성 제어(LK__)
     */
    LOCK_TIMEOUT("LK01", "동시성 제어 락 획득에 실패했습니다."),

    /**
     * 서버, 그 외
     */
    FIELD_NOT_VALID("FD01", "유효하지 않은 필드 값입니다."),
    FIELD_TYPE_NOT_VALID("FD02", "유효하지 않은 필드 타입입니다."),
    SERVER_ERROR("SV01", "서버 에러가 발생했습니다.");

    private final String code;
    private final String message;
}