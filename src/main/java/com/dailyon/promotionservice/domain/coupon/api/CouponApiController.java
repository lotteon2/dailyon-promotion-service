package com.dailyon.promotionservice.domain.coupon.api;

import com.dailyon.promotionservice.domain.coupon.api.request.CouponCreateRequest;
import com.dailyon.promotionservice.domain.coupon.service.CouponService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Slf4j
@RequiredArgsConstructor
@RestController("/coupons")
public class CouponApiController {
    private final CouponService couponService;

    @PostMapping("") // 생성 후 생성된 리소스 바로 접근할 수 있게 id값을 반환.
    public ResponseEntity<Long> createCouponInfoWithAppliesTo(@Valid @RequestBody CouponCreateRequest request) {
        if (!request.isValidDiscount()) {
            throw new IllegalArgumentException("Invalid discount: either rate or amount must be set, not both");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(couponService.createCouponInfoWithAppliesTo(request));
    }
}
