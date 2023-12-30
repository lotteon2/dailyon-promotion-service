package com.dailyon.promotionservice.domain.coupon.repository.custom;

import com.dailyon.promotionservice.domain.coupon.entity.MemberCoupon;

import java.util.List;

public interface MemberCouponRepositoryCustom {
    List<MemberCoupon> findActiveAndUnusedCouponsByMemberId(Long memberId);

    List<MemberCoupon> findMemberCouponsByMemberIdAndCouponInfoIds(Long memberId, List<Long> couponInfoIds);
}
