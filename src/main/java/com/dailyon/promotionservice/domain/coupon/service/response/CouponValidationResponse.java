package com.dailyon.promotionservice.domain.coupon.service.response;

import com.dailyon.promotionservice.domain.coupon.entity.CouponInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public class CouponValidationResponse {
    private Long productId;
    private String discountType;
    private Long discountValue;
    private String couponName;
    private Long couponInfoId;
    private Long minPurchaseAmount;
    private Long maxDiscountAmount;

    public static CouponValidationResponse from(Long productId, CouponInfo couponInfo) {
        return CouponValidationResponse.builder()
                .productId(productId)
                .discountType(couponInfo.getDiscountType().toString())
                .discountValue(couponInfo.getDiscountValue())
                .couponName(couponInfo.getName())
                .couponInfoId(couponInfo.getId())
                .minPurchaseAmount(couponInfo.getMinPurchaseAmount())
                .maxDiscountAmount(couponInfo.getMaxDiscountAmount())
                .build();

    }
}
