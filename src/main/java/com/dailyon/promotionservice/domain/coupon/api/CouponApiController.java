package com.dailyon.promotionservice.domain.coupon.api;

import com.dailyon.promotionservice.domain.coupon.api.request.CouponCreateRequest;
import com.dailyon.promotionservice.domain.coupon.api.request.CouponModifyRequest;
import com.dailyon.promotionservice.domain.coupon.api.request.MultipleProductsCouponRequest;
import com.dailyon.promotionservice.domain.coupon.service.response.CouponExistenceResponse;
import com.dailyon.promotionservice.domain.coupon.exceptions.InvalidDateRangeException;
import com.dailyon.promotionservice.domain.coupon.exceptions.InvalidDiscountException;
import com.dailyon.promotionservice.domain.coupon.service.CouponService;
import com.dailyon.promotionservice.domain.coupon.service.response.CouponInfoItemResponse;
import com.dailyon.promotionservice.domain.coupon.service.response.MultipleProductCouponsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/coupons")
public class CouponApiController {
    private final CouponService couponService;

    @PostMapping("") // 생성 후 생성된 리소스 바로 접근할 수 있게 id값을 반환.
    public ResponseEntity<Long> createCouponInfoWithAppliesTo(@Valid @RequestBody CouponCreateRequest request) {
        String invalidDiscountMessage = request.getInvalidDiscountMessage();
        if (invalidDiscountMessage != null) {
            throw new InvalidDiscountException(invalidDiscountMessage);
        }
        if (!request.isValidDateRange()) {
            throw new InvalidDateRangeException();
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(couponService.createCouponInfoWithAppliesTo(request));
    }

    @PatchMapping("/{couponInfoId}") // 바꾼 리소스의 id값을 반환
    public ResponseEntity<Long> modifyCouponInfo(@PathVariable Long couponInfoId,
                                                           @Valid @RequestBody CouponModifyRequest request) {
        String invalidDiscountMessage = request.getInvalidDiscountMessage();
        if (invalidDiscountMessage != null) {
            throw new InvalidDiscountException(invalidDiscountMessage);
        }
        if (!request.isValidDateRange()) {
            throw new InvalidDateRangeException();
        }
        Long updatedCouponId = couponService.modifyCouponInfo(request, couponInfoId);

        return ResponseEntity.ok(updatedCouponId);
    }


    @DeleteMapping("/{couponInfoId}")
    public ResponseEntity<Void> deleteCouponInfoWithAppliesTo(@PathVariable Long couponInfoId) {
        couponService.deleteCouponInfoWithAppliesTo(couponInfoId);
        return ResponseEntity.noContent().build();
    }


    @PatchMapping("/{couponInfoId}/invalidate") // 바꾼 리소스의 id값을 반환
    public ResponseEntity<Long> invalidateCoupon(@PathVariable Long couponInfoId) {
        Long invalidatedCouponId = couponService.invalidateCoupon(couponInfoId);

        return ResponseEntity.ok(invalidatedCouponId);
    }

    @GetMapping("/coupons-existence")
    public ResponseEntity<List<CouponExistenceResponse>> checkCouponsExistenceByProductIds(@RequestParam List<Long> productIds) {
        List<CouponExistenceResponse> couponExistenceList = couponService.checkCouponsExistenceByProductIds(productIds);
        return ResponseEntity.ok(couponExistenceList);
    }

    @GetMapping(value = "/single-product", params = {"productId", "categoryId"} ) // 둘 다 받아야함을 명시.
    public ResponseEntity<List<CouponInfoItemResponse>> getSingleProductCoupon(@RequestParam long productId, @RequestParam long categoryId) {
        List<CouponInfoItemResponse> couponExistenceList = couponService.getActiveCouponsForProductAndCategory(productId, categoryId);
        return ResponseEntity.ok(couponExistenceList);
    }

    @PostMapping(value = "/multiple-products")
    public ResponseEntity<MultipleProductCouponsResponse> getMultipleProductsCoupons(
            @RequestBody MultipleProductsCouponRequest request) {

        MultipleProductCouponsResponse couponsMap = couponService.getActiveCouponsForMultipleProductsAndCategories(request);
        return ResponseEntity.ok(couponsMap);
    }

    @GetMapping(params = "categoryId")
    public ResponseEntity<List<CouponInfoItemResponse>> getCategoryCoupon(@RequestParam long categoryId) {
        List<CouponInfoItemResponse> couponExistenceList = couponService.getActiveCouponsForCategory(categoryId);
        return ResponseEntity.ok(couponExistenceList);
    }


}
