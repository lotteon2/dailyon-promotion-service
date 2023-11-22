package com.dailyon.promotionservice.domain.coupon.repository;

import com.dailyon.promotionservice.domain.coupon.entity.CouponAppliesTo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CouponAppliesToRepository extends JpaRepository<CouponAppliesTo, Long> {
}
