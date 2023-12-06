package com.dailyon.promotionservice.domain.coupon.service;


import com.dailyon.promotionservice.common.exceptions.ErrorResponseException;
import com.dailyon.promotionservice.domain.coupon.api.request.CouponCreateRequest;
import com.dailyon.promotionservice.domain.coupon.api.request.CouponModifyRequest;
import com.dailyon.promotionservice.domain.coupon.api.request.CouponValidationRequest;
import com.dailyon.promotionservice.domain.coupon.api.request.MultipleProductsCouponRequest;
import com.dailyon.promotionservice.domain.coupon.entity.MemberCoupon;
import com.dailyon.promotionservice.domain.coupon.service.response.CouponExistenceResponse;
import com.dailyon.promotionservice.domain.coupon.entity.CouponAppliesTo;
import com.dailyon.promotionservice.domain.coupon.entity.CouponInfo;
import com.dailyon.promotionservice.domain.coupon.entity.CouponTargetType;
import com.dailyon.promotionservice.domain.coupon.repository.CouponAppliesToRepository;
import com.dailyon.promotionservice.domain.coupon.repository.CouponInfoRepository;
import com.dailyon.promotionservice.domain.coupon.repository.MemberCouponRepository;
import com.dailyon.promotionservice.domain.coupon.service.response.CouponInfoItemResponse;
import com.dailyon.promotionservice.domain.coupon.service.response.CouponValidationResponse;
import com.dailyon.promotionservice.domain.coupon.service.response.MultipleProductCouponsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.*;
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
                                .hasAvailableCoupon(productIdsWithCoupons.contains(productId))
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

    // TODO: 동시성 이슈 처리
    @Transactional
    public void downloadCoupon(Long memberId, Long couponId) {
        CouponInfo couponInfo = couponInfoRepository.findById(couponId)
                .orElseThrow(() -> new EntityNotFoundException("Coupon not found"));

        couponInfo.decreaseRemainingQuantity();

        MemberCoupon memberCoupon = MemberCoupon.builder()
                .memberId(memberId)
                .couponInfoId(couponInfo.getId())
                .createdAt(LocalDateTime.now())
                .isUsed(false)
                .build();
        memberCouponRepository.save(memberCoupon);
    }

    @Transactional
    public void useCoupon(Long memberId, Long couponId) {
        MemberCoupon.MemberCouponId memberCouponId = MemberCoupon.MemberCouponId.builder()
                .memberId(memberId)
                .couponInfoId(couponId)
                .build();
        MemberCoupon memberCoupon = memberCouponRepository.findById(memberCouponId).orElseThrow(() ->
                new EntityNotFoundException("MemberCoupon not found for memberId " + memberId + " and couponId " + couponId)
        );

        if (memberCoupon.getIsUsed()) {
            throw new ErrorResponseException("이미 사용된 쿠폰입니다.");
        }

        /*
        임박해서 쓸수도 있으니, 조회는 endAt을 기준으로 하고, 사용은 1시간 더 여유를 둠.
        조회를 못하는 쿠폰은 클라이언트에서 사용하겠다는 요청을 보낼 수 없으니,
        주문창에 이미 들어온 임박건을 위한 조건임.
         */
        CouponInfo couponInfo = memberCoupon.getCouponInfo();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expirationTimeWithGracePeriod = couponInfo.getEndAt().plusHours(1);
        if (now.isBefore(couponInfo.getStartAt()) || now.isAfter(expirationTimeWithGracePeriod)) {
            throw new IllegalStateException("쿠폰의 유효기간이 지났습니다.");
        }

        memberCoupon.markAsUsed();
    }

    public List<CouponValidationResponse> validateCoupons(Long memberId, List<CouponValidationRequest> request) {
        List<Long> couponInfoIds = new ArrayList<>();
        for (CouponValidationRequest couponValidationRequest : request) {
            couponInfoIds.add(couponValidationRequest.getCouponInfoId());
        }

        // memberId와 couponInfoIds로 쿠폰 정보를 가져옴
        List<MemberCoupon> memberCoupons = memberCouponRepository.findByMemberIdAndCouponInfoIdIn(memberId, couponInfoIds);

        // unique key(memberId, couponInfoId)들로 가져온 쿠폰들의 개수가 맞지 않으면,
        // 어떤 특정 couponInfoId와 memberId의 조합이 유효하지 않다는 뜻.
        if (memberCoupons.size() != request.size()) {
            throw new ErrorResponseException("유효하지 않은 쿠폰정보를 입력했습니다.");
        }

        // productId를 key로 관리하기 위한 맵 생성
        Map<Long, CouponInfo> couponInfoMap = new HashMap<>();
        for (MemberCoupon memberCoupon : memberCoupons) {
            CouponInfo couponInfo = memberCoupon.getCouponInfo();
            couponInfoMap.put(couponInfo.getId(), couponInfo); // couponInfoId를 key로 사용
        }

        // 요청된 순서대로 결과를 매핑하고 반환
        List<CouponValidationResponse> responses = new ArrayList<>();
        for (CouponValidationRequest couponValidationRequest : request) {
            CouponInfo couponInfo = couponInfoMap.get(couponValidationRequest.getCouponInfoId());
            if (couponInfo != null) {
                responses.add(CouponValidationResponse.from(couponValidationRequest.getProductId(), couponInfo));
            } else {
                throw new ErrorResponseException("연관된 쿠폰 정보가 존재하지 않습니다.");
            }

            if (couponInfo.getEndAt().isBefore(LocalDateTime.now().minusHours(1))) {
                // 사용에 한해서는 1시간의 여유를 더 줌. 조회는 endAt에서 끝.
                throw new ErrorResponseException("사용 유효기간이 지난 쿠폰입니다.");
            }
        }
        return responses;
    }
}
