package com.dailyon.promotionservice.domain.coupon.exceptions.handler;

import com.dailyon.promotionservice.domain.coupon.api.CouponApiController;
import com.dailyon.promotionservice.domain.coupon.exceptions.InvalidDiscountException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = {CouponApiController.class})
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CouponDomainExceptionHandler {

    @ExceptionHandler(InvalidDiscountException.class)
    public ResponseEntity<String> handleInvalidDiscount(InvalidDiscountException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    // Coupon 도메인의 다른 예외들에 대한 핸들러들...
}