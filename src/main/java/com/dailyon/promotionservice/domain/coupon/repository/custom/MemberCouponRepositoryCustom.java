package com.dailyon.promotionservice.domain.coupon.repository.custom;

import com.dailyon.promotionservice.domain.coupon.entity.MemberCoupon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MemberCouponRepositoryCustom {
    List<MemberCoupon> findActiveAndUnusedCouponsByMemberId(Long memberId);

    List<MemberCoupon> findMemberCouponsByMemberIdAndCouponInfoIds(Long memberId, List<Long> couponInfoIds);

    Page<MemberCoupon> findMemberCouponsWithCouponInfoByMemberId(Long memberId, Pageable pageable);
}
