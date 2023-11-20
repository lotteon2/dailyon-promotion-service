package com.dailyon.promotionservice.domain.coupon.service;

import com.dailyon.promotionservice.domain.coupon.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class CouponService {

    private final CouponRepository couponRepository;
}
