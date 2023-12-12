package com.dailyon.promotionservice.domain.coupon.service.response;

import com.dailyon.promotionservice.domain.coupon.entity.CouponInfo;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.SuperBuilder;


@Getter
@SuperBuilder // 상속관계 받기위해 @Builder 대체하는 lombok의 annotation
@EqualsAndHashCode(callSuper = true)
public class CouponInfoItemWithAvailabilityResponse extends CouponInfoItemResponse {
    private Boolean isDownloadable;

    @Override
    public String toString() {
        return "CouponInfoItemWithAvailabilityResponse(" +
                "super=" + super.toString() +
                ", isDownloadable=" + isDownloadable +
                ")";
    }

    public static CouponInfoItemWithAvailabilityResponse from(CouponInfo couponInfo, Boolean isDownloadable) {
        // 일반적인 빌더와 다름.
        return builder()
                .couponInfoId(couponInfo.getId())
                .appliesToType(couponInfo.getAppliesTo().getAppliesToType())
                .appliedToId(couponInfo.getAppliesTo().getAppliesToId())
                .discountType(couponInfo.getDiscountType())
                .discountValue(couponInfo.getDiscountValue())
                .endAt(couponInfo.getEndAt())
                .isDownloadable(isDownloadable)
                .build();
    }
}
