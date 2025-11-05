package com.example.ecommerceapi.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    /**
     * 상품 (PD__)
     */
    PRODUCT_NOT_FOUND("PD01", "존재하는 상품이 아닙니다.", 404),
    PRODUCT_NOT_VALID_STATISTIC("PD02", "지원하지 않는 상품 통계 타입입니다", 400),

    /**
     * 주문/결제(OD__)
     */

    /**
     * 장바구니(CT__)
     */
    CART_INVALID_QUANTITY("CT01", "수량은 1 이상이어야 합니다.", 400),
    CART_EXCEED_STOCK("CT02", "장바구니 수량이 남은 재고를 초과했습니다.", 400),
    CART_ITEM_NOT_FOUND("CT03", "존재하지 않는 장바구니 상품 ID 입니다.", 404),

    /**
     * 쿠폰(CP__)
     */
    COUPON_NOT_FOUND("CP01", "존재하지 않는 쿠폰입니다.", 404),
    COUPON_NOT_AVAILABLE("CP02", "발급 가능한 쿠폰이 아닙니다. 수량이 소진되었거나 만료되었습니다.", 400),
    COUPON_ALREADY_ISSUED("CP03", "이미 발급받은 쿠폰입니다.", 400),

    /**
     * 회원(US__)
     */
    USER_NOT_FOUND("US01", "회원이 존재하지 않습니다.", 404),

    /**
     * 포인트(PT__)
     */
    POINT_INSUFFICIENT_BALANCE("PT01", "포인트 잔액이 부족합니다.", 400),
    POINT_INVALID_AMOUNT("PT02", "포인트 금액이 유효하지 않습니다.", 400),
    POINT_CHARGE_IN_PROGRESS("PT03", "포인트 충전 요청이 처리 중입니다. 잠시 후 다시 시도해주세요.", 409),

    /**
     * 동시성 제어(LK__)
     */
    LOCK_TIMEOUT("LK01", "동시성 제어 락 획득에 실패했습니다.", 409),

    /**
     * 서버, 그 외
     */
    FIELD_NOT_VALID("FD01", "유효하지 않은 필드 값입니다.", 400),
    FIELD_TYPE_NOT_VALID("FD02", "유효하지 않은 필드 타입입니다.",400),
    SERVER_ERROR("SV01", "서버 에러가 발생했습니다.", 500);

    private final String code;
    private final String message;
    private final Integer status;
}