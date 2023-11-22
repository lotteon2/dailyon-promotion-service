package com.dailyon.promotionservice.domain.coupon.service;


import com.dailyon.promotionservice.domain.coupon.repository.CouponAppliesToRepository;
import com.dailyon.promotionservice.domain.coupon.repository.CouponInfoRepository;
import com.dailyon.promotionservice.domain.coupon.repository.MemberCouponRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class CouponService {

    private final CouponInfoRepository couponInfoRepository;
    private final CouponAppliesToRepository couponAppliesToRepository;
    private final MemberCouponRepository memberCouponRepository;
}
