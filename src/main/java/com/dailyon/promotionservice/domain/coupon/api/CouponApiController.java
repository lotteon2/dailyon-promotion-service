package com.dailyon.promotionservice.domain.coupon.api;

import com.dailyon.promotionservice.domain.coupon.api.request.CouponCreateRequest;
import com.dailyon.promotionservice.domain.coupon.api.request.CouponModifyRequest;
import com.dailyon.promotionservice.domain.coupon.exceptions.InvalidDiscountException;
import com.dailyon.promotionservice.domain.coupon.service.CouponService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/coupons")
public class CouponApiController {
    private final CouponService couponService;

    @PostMapping("") // 생성 후 생성된 리소스 바로 접근할 수 있게 id값을 반환.
    public ResponseEntity<Long> createCouponInfoWithAppliesTo(@Valid @RequestBody CouponCreateRequest request) {
        if (!request.isValidDiscount()) {
            throw new InvalidDiscountException();
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(couponService.createCouponInfoWithAppliesTo(request));
    }

    @PatchMapping("/{couponInfoId}") // 바꾼 리소스의 id값을 반환
    public ResponseEntity<Long> modifyCouponInfo(@PathVariable Long couponInfoId,
                                                           @Valid @RequestBody CouponModifyRequest request) {
        if (!request.isValidDiscount()) {
            throw new InvalidDiscountException();
        }
        Long updatedCouponId = couponService.modifyCouponInfo(request, couponInfoId);

        return ResponseEntity.ok(updatedCouponId);
    }
}
