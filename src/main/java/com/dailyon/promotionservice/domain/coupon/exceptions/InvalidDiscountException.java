package com.dailyon.promotionservice.domain.coupon.exceptions;

public class InvalidDiscountException extends RuntimeException {
    private static final String DEFAULT_MESSAGE = "Invalid discount: either rate or amount must be set, not both";

    public InvalidDiscountException() {
        super(DEFAULT_MESSAGE);
    }

    // 에러 메세지 커스텀 필요하면 오버로딩 생성자 사용.
    public InvalidDiscountException(String message) {
        super(message);
    }
}