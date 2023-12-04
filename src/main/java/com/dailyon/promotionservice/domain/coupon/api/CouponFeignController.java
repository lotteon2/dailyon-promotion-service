package com.dailyon.promotionservice.domain.coupon.api;

import com.dailyon.promotionservice.domain.coupon.service.CouponService;
import com.dailyon.promotionservice.domain.coupon.service.response.CouponExistenceResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/clients/coupons")
public class CouponFeignController {
    private final CouponService couponService;

    @GetMapping("/coupons-existence")
    public ResponseEntity<List<CouponExistenceResponse>> checkCouponsExistenceByProductIds(@RequestParam List<Long> productIds) {
        List<CouponExistenceResponse> couponExistenceList = couponService.checkCouponsExistenceByProductIds(productIds);
        return ResponseEntity.ok(couponExistenceList);
    }
}
