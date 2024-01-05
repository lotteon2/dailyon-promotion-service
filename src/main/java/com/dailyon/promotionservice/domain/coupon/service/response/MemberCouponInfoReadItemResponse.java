package com.dailyon.promotionservice.domain.coupon.service.response;

import com.dailyon.promotionservice.domain.coupon.entity.CouponInfo;
import com.dailyon.promotionservice.domain.coupon.entity.MemberCoupon;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class MemberCouponInfoReadItemResponse {

    private Long id;
    private String name;
    private String discountType;
    private Long discountValue;

    private String startAt;
    private String endAt;
    private String createdAt;

    private String appliesToType;
    private Long appliesToId;
    private String appliesToName;

    private Boolean requiresConcurrencyControl;

    private String targetImgUrl;
    private Long minPurchaseAmount;
    private Long maxDiscountAmount;

    public static MemberCouponInfoReadItemResponse ofEntities(CouponInfo couponInfo, MemberCoupon memberCoupon) {
        return MemberCouponInfoReadItemResponse.builder()
                .id(couponInfo.getId())
                .name(couponInfo.getName())
                .discountType(couponInfo.getDiscountType().toString())
                .discountValue(couponInfo.getDiscountValue())
                .startAt(couponInfo.getStartAt().toString())
                .endAt(couponInfo.getEndAt().toString())
                .createdAt(memberCoupon.getCreatedAt().toString())
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
