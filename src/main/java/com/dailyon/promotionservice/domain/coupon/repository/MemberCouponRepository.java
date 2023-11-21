package com.dailyon.promotionservice.domain.coupon.repository;

import com.dailyon.promotionservice.domain.coupon.entity.MemberCoupon;
import org.springframework.data.jpa.repository.JpaRepository;


public interface MemberCouponRepository extends JpaRepository<MemberCoupon, MemberCoupon.MemberCouponId> {
}
