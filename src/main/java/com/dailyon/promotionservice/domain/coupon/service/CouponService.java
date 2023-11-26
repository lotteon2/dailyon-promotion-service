package com.dailyon.promotionservice.domain.coupon.service;


import com.dailyon.promotionservice.domain.coupon.api.request.CouponCreateRequest;
import com.dailyon.promotionservice.domain.coupon.api.request.CouponModifyRequest;
import com.dailyon.promotionservice.domain.coupon.entity.CouponAppliesTo;
import com.dailyon.promotionservice.domain.coupon.entity.CouponInfo;
import com.dailyon.promotionservice.domain.coupon.entity.CouponTargetType;
import com.dailyon.promotionservice.domain.coupon.repository.CouponAppliesToRepository;
import com.dailyon.promotionservice.domain.coupon.repository.CouponInfoRepository;
import com.dailyon.promotionservice.domain.coupon.repository.MemberCouponRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class CouponService {

    private final CouponInfoRepository couponInfoRepository;
    private final CouponAppliesToRepository couponAppliesToRepository;
    private final MemberCouponRepository memberCouponRepository;

    @Transactional
    public Long createCouponInfoWithAppliesTo(CouponCreateRequest request) {
        CouponInfo couponInfo = couponInfoRepository.save(request.toEntity());
        CouponTargetType appliesToType = CouponTargetType.fromString(request.getAppliesToType());
        
        CouponAppliesTo appliesTo = CouponAppliesTo.createWithCouponInfo(
                couponInfo,
                request.getAppliesToId(),
                appliesToType
        );
        couponAppliesToRepository.save(appliesTo);
        return couponInfo.getId();
    }

    @Transactional
    public Long modifyCouponInfo(CouponModifyRequest request, Long couponInfoId) {
        CouponInfo couponInfo = couponInfoRepository.findById(couponInfoId)
                .orElseThrow(() -> new EntityNotFoundException("CouponInfo not found for id: " + couponInfoId));
        couponInfo.updateDetails(request);

        return couponInfo.getId();
    }

    @Transactional
    public void deleteCouponInfoWithAppliesTo(Long couponInfoId) {
        CouponInfo couponInfo = couponInfoRepository.findById(couponInfoId)
                .orElseThrow(() -> new EntityNotFoundException("CouponInfo not found with id: " + couponInfoId));

        // 연관된 CouponAppliesTo entity는 cascade에 의해 삭제됨.
        couponInfoRepository.delete(couponInfo);
    }
    


}
