package com.dailyon.promotionservice.domain.coupon.repository;

import com.dailyon.promotionservice.domain.coupon.entity.CouponAppliesTo;

import com.dailyon.promotionservice.domain.coupon.entity.CouponInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.swing.text.html.Option;
import java.util.Optional;

@Repository
public interface CouponAppliesToRepository extends JpaRepository<CouponAppliesTo, CouponInfo> {
    Optional<CouponAppliesTo> findByCouponInfoId(Long couponInfoId);
}
