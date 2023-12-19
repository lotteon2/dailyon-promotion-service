package com.dailyon.promotionservice.domain.coupon.repository;

import com.dailyon.promotionservice.domain.coupon.entity.MemberCoupon;
import com.dailyon.promotionservice.domain.coupon.repository.custom.MemberCouponRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MemberCouponRepository extends JpaRepository<MemberCoupon, MemberCoupon.MemberCouponId>, MemberCouponRepositoryCustom {
    List<MemberCoupon> findByMemberIdAndCouponInfoIdIn(Long memberId, List<Long> couponInfoIds);

    List<MemberCoupon> findByMemberId(Long memberId);
}
