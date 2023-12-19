package com.dailyon.promotionservice.domain.coupon.service.response;

import com.dailyon.promotionservice.domain.coupon.entity.CouponInfo;
import com.dailyon.promotionservice.domain.coupon.entity.MemberCoupon;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CouponInfoItemCheckoutResponse extends CouponInfoItemResponse {
    private String couponInfoName;

    @Override
    public String toString() {
        return "CouponInfoItemApplied(" +
                "super=" + super.toString() +
                ", couponInfoName=" + couponInfoName +
                ")";
    }

    public static CouponInfoItemCheckoutResponse from(CouponInfo couponInfo) {
        return builder()
                .couponInfoId(couponInfo.getId())
                .couponInfoName(couponInfo.getName())
                .appliesToType(couponInfo.getAppliesTo().getAppliesToType())
                .appliedToId(couponInfo.getAppliesTo().getAppliesToId())
                .discountType(couponInfo.getDiscountType())
                .discountValue(couponInfo.getDiscountValue())
                .endAt(couponInfo.getEndAt())
                .minPurchaseAmount(couponInfo.getMinPurchaseAmount())
                .maxDiscountAmount(couponInfo.getMaxDiscountAmount())
                .build();
    }
}