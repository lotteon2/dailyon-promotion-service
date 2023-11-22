package com.dailyon.promotionservice.domain.coupon.api;

import com.dailyon.promotionservice.domain.coupon.service.CouponService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
public class CouponApiController {
    private final CouponService couponService;
}
