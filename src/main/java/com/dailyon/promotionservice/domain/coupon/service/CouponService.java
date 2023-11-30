package com.dailyon.promotionservice.domain.coupon.service;


import com.dailyon.promotionservice.domain.coupon.api.request.CouponCreateRequest;
import com.dailyon.promotionservice.domain.coupon.api.request.CouponModifyRequest;
import com.dailyon.promotionservice.domain.coupon.api.request.MultipleProductsCouponRequest;
import com.dailyon.promotionservice.domain.coupon.service.response.CouponExistenceResponse;
import com.dailyon.promotionservice.domain.coupon.entity.CouponAppliesTo;
import com.dailyon.promotionservice.domain.coupon.entity.CouponInfo;
import com.dailyon.promotionservice.domain.coupon.entity.CouponTargetType;
import com.dailyon.promotionservice.domain.coupon.repository.CouponAppliesToRepository;
import com.dailyon.promotionservice.domain.coupon.repository.CouponInfoRepository;
import com.dailyon.promotionservice.domain.coupon.repository.MemberCouponRepository;
import com.dailyon.promotionservice.domain.coupon.service.response.CouponInfoItemResponse;
import com.dailyon.promotionservice.domain.coupon.service.response.MultipleProductCouponsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

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
        CouponAppliesTo couponAppliesTo = couponAppliesToRepository.findByCouponInfoId(couponInfoId)
                .orElseThrow(() -> new EntityNotFoundException("CouponInfo not found with id: " + couponInfoId)
        );

        // 연관된 CouponAppliesTo entity는 cascade에 의해 삭제됨.
        couponAppliesToRepository.delete(couponAppliesTo);

    }


    @Transactional
    public Long invalidateCoupon(Long couponInfoId) {
        CouponInfo couponInfo = couponInfoRepository.findById(couponInfoId)
                .orElseThrow(() -> new EntityNotFoundException("CouponInfo not found with id: " + couponInfoId));

        couponInfo.invalidateCoupon();

        return couponInfoId;
    }

    public List<CouponExistenceResponse> checkCouponsExistenceByProductIds(List<Long> productIds) {
        Set<Long> productIdsWithCoupons = couponInfoRepository.findProductIdsWithCoupons(productIds);

        return productIds.stream()
                .map(productId ->
                        CouponExistenceResponse.builder()
                                .productId(productId)
                                .hasCoupons(productIdsWithCoupons.contains(productId))
                                .build())
                .collect(Collectors.toList());
    }

    public MultipleProductCouponsResponse getActiveCouponsForMultipleProductsAndCategories(MultipleProductsCouponRequest request) {
        // 상품 ID와 카테고리 ID 목록 추출
        List<Long> productIds = request.getProducts().stream()
                .map(MultipleProductsCouponRequest.ProductCategoryPair::getProductId)
                .collect(Collectors.toList());
        Set<Long> categoryIds = request.getProducts().stream()
                .map(MultipleProductsCouponRequest.ProductCategoryPair::getCategoryId)
                .collect(Collectors.toSet());

        List<CouponInfo> coupons = couponInfoRepository.findActiveCouponsForProductsAndCategories(productIds, new ArrayList<>(categoryIds));

        // 상품 ID를 키로 하고, 쿠폰 리스트를 값으로 갖는 맵을 초기화
        Map<Long, List<CouponInfoItemResponse>> productCouponsMap = productIds.stream()
                .collect(Collectors.toMap(Function.identity(), id -> new ArrayList<>()));

        for (MultipleProductsCouponRequest.ProductCategoryPair productCategoryPair : request.getProducts()) {
            Long productId = productCategoryPair.getProductId();
            Long categoryId = productCategoryPair.getCategoryId();

            for (CouponInfo couponInfo : coupons) {
                Long appliesToId = couponInfo.getAppliesTo().getAppliesToId();
                CouponTargetType appliesToType = couponInfo.getAppliesTo().getAppliesToType();

                if (( appliesToType == CouponTargetType.PRODUCT && appliesToId.equals(productId)) ||
                        ( appliesToType == CouponTargetType.CATEGORY && appliesToId.equals(categoryId))) {
                    productCouponsMap.get(productId).add(CouponInfoItemResponse.from(couponInfo));
                }
            }
        }
        return new MultipleProductCouponsResponse(productCouponsMap);
    }

    public List<CouponInfoItemResponse> getActiveCouponsForProductAndCategory(long productId, long categoryId) {
        List<CouponInfo> coupons = couponInfoRepository.findActiveCouponsForProductAndCategory(productId, categoryId);

        return coupons.stream().map(couponInfo -> CouponInfoItemResponse.builder()
                .appliesToType(couponInfo.getAppliesTo().getAppliesToType())
                .appliedToId(couponInfo.getAppliesTo().getAppliesToId())
                .discountType(couponInfo.getDiscountType())
                .discountValue(couponInfo.getDiscountValue())
                .endAt(couponInfo.getEndAt())
                .build()
        ).collect(Collectors.toList());
    }

    public List<CouponInfoItemResponse> getActiveCouponsForCategory(Long categoryId) {
        List<CouponInfo> activeCoupons = couponInfoRepository.findActiveCouponsForCategory(categoryId);
        return activeCoupons.stream()
                .map(couponInfo -> CouponInfoItemResponse.builder()
                        .appliesToType(CouponTargetType.CATEGORY)
                        .appliedToId(couponInfo.getAppliesTo().getAppliesToId())
                        .discountType(couponInfo.getDiscountType())
                        .discountValue(couponInfo.getDiscountValue())
                        .endAt(couponInfo.getEndAt())
                        .build())
                .collect(Collectors.toList());
    }


}
