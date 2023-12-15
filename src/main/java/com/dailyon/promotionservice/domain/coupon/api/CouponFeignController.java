package com.dailyon.promotionservice.domain.coupon.api;

import com.dailyon.promotionservice.domain.coupon.api.request.CouponValidationRequest;
import com.dailyon.promotionservice.domain.coupon.api.request.MultipleProductsCouponRequest;
import com.dailyon.promotionservice.domain.coupon.service.CouponService;
import com.dailyon.promotionservice.domain.coupon.service.response.CouponExistenceResponse;
import com.dailyon.promotionservice.domain.coupon.service.response.CouponValidationResponse;
import com.dailyon.promotionservice.domain.coupon.service.response.MultipleProductCouponsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
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

    @PostMapping(value = "/multiple-products")
    public ResponseEntity<MultipleProductCouponsResponse> getMultipleProductsCoupons(
            @RequestBody MultipleProductsCouponRequest request) {

        MultipleProductCouponsResponse couponsMap = couponService.getActiveCouponsForMultipleProductsAndCategories(request);
        return ResponseEntity.ok(couponsMap);
    }

    @PostMapping("/validate-for-order")
    public ResponseEntity<List<CouponValidationResponse>> validateCouponsForOrder(
            @RequestHeader Long memberId,
            @Valid @RequestBody List<CouponValidationRequest> request) {

        List<CouponValidationResponse> validationResponses = couponService.validateCouponsForOrder(memberId, request);
        return ResponseEntity.ok(validationResponses);
    }
}
