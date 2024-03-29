package com.dailyon.promotionservice.domain.coupon.api;

import com.dailyon.promotionservice.domain.coupon.api.request.CouponCreateRequest;
import com.dailyon.promotionservice.domain.coupon.api.request.CouponModifyRequest;
import com.dailyon.promotionservice.domain.coupon.api.request.MultipleProductsCouponRequest;
import com.dailyon.promotionservice.domain.coupon.service.response.*;
import com.dailyon.promotionservice.domain.coupon.exceptions.InvalidDateRangeException;
import com.dailyon.promotionservice.domain.coupon.exceptions.InvalidDiscountException;
import com.dailyon.promotionservice.domain.coupon.service.CouponService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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

    // @GetMapping("/coupons-existence")
    // public ResponseEntity<List<CouponExistenceResponse>> checkCouponsExistenceByProductIds(@RequestParam List<Long> productIds) {
    //     List<CouponExistenceResponse> couponExistenceList = couponService.checkCouponsExistenceByProductIds(productIds);
    //     return ResponseEntity.ok(couponExistenceList);
    // }

    @GetMapping("/my-coupons")
    public ResponseEntity<MemberCouponInfoReadPageResponse> getMyCoupons(
            @RequestHeader long memberId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        MemberCouponInfoReadPageResponse response = couponService.getMemberCouponsPage(memberId, pageable);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/multiple-products") // listView를 위함. memberId 필요없음. client에서 조회
    public ResponseEntity<MultipleProductCouponsResponse> getMultipleProductsCoupons(
            @RequestBody MultipleProductsCouponRequest request) {

        MultipleProductCouponsResponse couponsMap = couponService.getActiveCouponsForMultipleProductsAndCategories(request);
        return ResponseEntity.ok(couponsMap);
    }

    @GetMapping(value = "/single-product", params = {"productId", "categoryId"} ) // 둘 다 받아야함을 명시.
    public ResponseEntity<List<CouponInfoItemResponse>> getSingleProductCoupon(@RequestParam long productId, @RequestParam long categoryId) {
        List<CouponInfoItemResponse> couponExistenceList = couponService.getActiveCouponsForProductAndCategory(productId, categoryId);
        return ResponseEntity.ok(couponExistenceList);
    }

    @GetMapping(value = "/single-product/with-availability", params = {"productId", "categoryId"} ) // 둘 다 받아야함을 명시. memberId 없을때 상품상세에서 최적혜택가를 보여주기 위함
    public ResponseEntity<List<CouponInfoItemWithAvailabilityResponse>> getSingleProductCouponWithAvailability(@RequestHeader(required = false) Long memberId,
            @RequestParam long productId, @RequestParam long categoryId) {

        List<CouponInfoItemWithAvailabilityResponse> couponExistenceList = couponService.getActiveCouponsForProductAndCategoryWithAvailability(memberId, productId, categoryId);
        return ResponseEntity.ok(couponExistenceList);
    }

    @GetMapping(params = "categoryId")
    public ResponseEntity<List<CouponInfoItemResponse>> getCategoryCoupon(@RequestParam long categoryId) {
        List<CouponInfoItemResponse> couponExistenceList = couponService.getActiveCouponsForCategory(categoryId);
        return ResponseEntity.ok(couponExistenceList);
    }

    // 쿠폰 다운로드
    @PostMapping("/{coupon_id}/download")
    public ResponseEntity<String> downloadCoupon(@RequestHeader("memberId") Long memberId,
                                            @PathVariable("coupon_id") Long couponId) {
        couponService.downloadCoupon(memberId, couponId);
        return ResponseEntity.ok("성공적으로 쿠폰을 다운로드 했습니다.");
    }

    @PostMapping("/download-multiple")
    public ResponseEntity<MultipleCouponDownloadResponse> downloadMultipleCoupons(@RequestHeader("memberId") Long memberId,
                                                                  @RequestBody List<Long> couponInfoIds) {
        MultipleCouponDownloadResponse multipleCouponDownloadResponse = couponService.downloadCoupons(memberId, couponInfoIds);
        return ResponseEntity.ok(multipleCouponDownloadResponse);
    }

    @PostMapping("/retrieve-applicable")
    public ResponseEntity<CheckoutCouponApplicationResponse> getCouponsForCheckout(@RequestHeader Long memberId,
                                                                                   @RequestBody MultipleProductsCouponRequest request) {
        CheckoutCouponApplicationResponse checkoutCouponApplicationResponse = couponService.findApplicableCoupons(memberId, request);
        return ResponseEntity.ok(checkoutCouponApplicationResponse);
    }



}
