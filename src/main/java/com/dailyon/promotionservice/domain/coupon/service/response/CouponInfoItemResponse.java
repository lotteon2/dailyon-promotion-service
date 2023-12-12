package com.dailyon.promotionservice.domain.coupon.service.response;

import com.dailyon.promotionservice.domain.coupon.entity.CouponInfo;
import com.dailyon.promotionservice.domain.coupon.entity.enums.CouponTargetType;
import com.dailyon.promotionservice.domain.coupon.entity.enums.DiscountType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder // 상속관계 받기위해 @Builder 대체하는 lombok의 annotation
@Data
public class CouponInfoItemResponse {
    private Long couponInfoId;
    private CouponTargetType appliesToType;
    private Long appliedToId;

    private DiscountType discountType;
    private Long discountValue;
    private LocalDateTime endAt;

    public static CouponInfoItemResponse from(CouponInfo couponInfo) {
        return CouponInfoItemResponse.builder()
                .couponInfoId(couponInfo.getId())
                .appliesToType(couponInfo.getAppliesTo().getAppliesToType())
                .appliedToId(couponInfo.getAppliesTo().getAppliesToId())
                .discountType(couponInfo.getDiscountType())
                .discountValue(couponInfo.getDiscountValue())
                .endAt(couponInfo.getEndAt())
                .build();
    }
}
