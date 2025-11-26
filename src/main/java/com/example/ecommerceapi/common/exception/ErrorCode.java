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
    PRODUCT_STOCK_INSUFFICIENT("PD03", "상품 재고가 부족합니다.", 409),
    PRODUCT_INVALID_STOCK_INCREMENT("PD04", "상품 재고 증가는 0 이하일 수 없습니다.", 400),

    /**
     * 주문/결제(OD__)
     */
    ORDER_NOT_FOUND("OD01", "존재하지 않는 주문입니다.", 404),
    ORDER_ALREADY_PAID("OD02", "이미 결제된 주문입니다.", 400),
    ORDER_PAY_INVALID_STATUS("OD03", "결제 가능한 주문 상태가 아닙니다.", 409),
    ORDER_PAY_FAILED("OD04", "결제 처리 중 오류가 발생했습니다.", 500),
    ORDER_ALREADY_EXISTS("OD05", "주문이 이미 존재합니다.", 400),

    /**
     * 장바구니(CT__)
     */
    CART_INVALID_QUANTITY("CT01", "수량은 1 이상이어야 합니다.", 400),
    CART_EXCEED_STOCK("CT02", "장바구니 수량이 남은 재고를 초과했습니다.", 400),
    CART_ITEM_NOT_FOUND("CT03", "존재하지 않는 장바구니 상품 ID 입니다.", 404),
    CART_EMPTY("CT04", "장바구니가 비어있습니다.", 400),

    /**
     * 쿠폰(CP__)
     */
    COUPON_NOT_FOUND("CP01", "존재하지 않는 쿠폰입니다.", 404),
    COUPON_NOT_AVAILABLE("CP02", "발급 가능한 쿠폰이 아닙니다. 수량이 소진되었거나 만료되었습니다.", 400),
    COUPON_ALREADY_ISSUED("CP03", "이미 발급받은 쿠폰입니다.", 400),
    COUPON_NOT_ISSUED("CP04", "발급받지 않은 쿠폰입니다.", 400),
    COUPON_ALREADY_USED("CP05", "이미 사용된 쿠폰입니다.", 400),
    COUPON_EXPIRED("CP06", "만료된 쿠폰입니다.", 400),

    /**
     * 회원(US__)
     */
    USER_NOT_FOUND("US01", "회원이 존재하지 않습니다.", 404),

    /**
     * 포인트(PT__)
     */
    POINT_INSUFFICIENT_BALANCE("PT01", "포인트 잔액이 부족합니다.", 409),
    POINT_INVALID_AMOUNT("PT02", "포인트 금액이 유효하지 않습니다.", 400),
    POINT_CHARGE_IN_PROGRESS("PT03", "포인트 충전 요청이 처리 중입니다. 잠시 후 다시 시도해주세요.", 409),
    POINT_RACE_CONDITION("PT04", "포인트 충전 요청이 중복됩니다. 잠시 후 다시 시도해주세요.", 409),

    /**
     * 동시성 제어(LK__)
     */
    LOCK_TIMEOUT("LK01", "동시성 제어 락 획득에 실패했습니다.", 409),
    LOCK_NOT_SUPPORTED("LK02", "단일 키로 호출을 지원하지 않습니다. 멀티키 방식을 사용해주세요.", 400),

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