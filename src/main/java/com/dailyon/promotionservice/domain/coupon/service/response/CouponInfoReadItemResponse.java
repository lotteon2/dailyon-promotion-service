package com.dailyon.promotionservice.domain.coupon.service.response;

import com.dailyon.promotionservice.domain.coupon.entity.CouponInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class CouponInfoReadItemResponse {
    private Long id;
    private String name;
    private String discountType;
    private Long discountValue;
    private String startAt;
    private String endAt;

    private Integer remainingQuantity;
    private Integer issuedQuantity; // CouponInfo Entity의 issuedQuantity

    private String appliesToType;
    private Long appliesToId;
    private String appliesToName;

    private Boolean requiresConcurrencyControl;

    private String targetImgUrl;
    private Long minPurchaseAmount;
    private Long maxDiscountAmount;


    public static CouponInfoReadItemResponse fromEntity(CouponInfo couponInfo) {
        return CouponInfoReadItemResponse.builder()
                .id(couponInfo.getId())
                .name(couponInfo.getName())
                .discountType(couponInfo.getDiscountType().toString())
                .discountValue(couponInfo.getDiscountValue())
                .startAt(couponInfo.getStartAt().toString())
                .endAt(couponInfo.getEndAt().toString())
                .remainingQuantity(couponInfo.getRemainingQuantity())
                .issuedQuantity(couponInfo.getIssuedQuantity())
                .appliesToType(couponInfo.getAppliesTo().getAppliesToType().toString())
                .appliesToId(couponInfo.getAppliesTo().getAppliesToId())
                .appliesToName(couponInfo.getAppliesTo().getAppliesToName())
                .requiresConcurrencyControl(couponInfo.getRequiresConcurrencyControl())
                .targetImgUrl(couponInfo.getTargetImgUrl())
                .minPurchaseAmount(couponInfo.getMinPurchaseAmount())
                .maxDiscountAmount(couponInfo.getMaxDiscountAmount())
                .build();
    }
}