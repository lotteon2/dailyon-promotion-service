package com.dailyon.promotionservice.domain.coupon.exceptions;

public class InvalidDateRangeException extends RuntimeException {
    private static final String DEFAULT_MESSAGE = "The start date must be before the end date";
    public InvalidDateRangeException() {
        super(DEFAULT_MESSAGE);
    }

    public InvalidDateRangeException(String message) {
        super(message);
    }
}