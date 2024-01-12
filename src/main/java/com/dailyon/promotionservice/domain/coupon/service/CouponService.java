package com.dailyon.promotionservice.domain.coupon.service;


import com.dailyon.promotionservice.common.exceptions.ErrorResponseException;
import com.dailyon.promotionservice.common.util.RedisDistributedLockManager;
import com.dailyon.promotionservice.domain.coupon.api.request.CouponCreateRequest;
import com.dailyon.promotionservice.domain.coupon.api.request.CouponModifyRequest;
import com.dailyon.promotionservice.domain.coupon.api.request.CouponValidationRequest;
import com.dailyon.promotionservice.domain.coupon.api.request.MultipleProductsCouponRequest;
import com.dailyon.promotionservice.domain.coupon.entity.MemberCoupon;
import com.dailyon.promotionservice.domain.coupon.infra.kafka.CouponEventProducer;
import com.dailyon.promotionservice.domain.coupon.infra.kafka.dto.OrderDTO;
import com.dailyon.promotionservice.domain.coupon.service.response.*;
import com.dailyon.promotionservice.domain.coupon.entity.CouponAppliesTo;
import com.dailyon.promotionservice.domain.coupon.entity.CouponInfo;
import com.dailyon.promotionservice.domain.coupon.entity.enums.CouponTargetType;
import com.dailyon.promotionservice.domain.coupon.repository.CouponAppliesToRepository;
import com.dailyon.promotionservice.domain.coupon.repository.CouponInfoRepository;
import com.dailyon.promotionservice.domain.coupon.repository.MemberCouponRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
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

    private final RedisDistributedLockManager lockManager;
    private final String couponDownloadLockPrefix = "Redisson_key_couponInfo:";

    private final CouponEventProducer couponEventProducer;
    private final RedissonClient redissonClient;

    @Transactional
    public Long createCouponInfoWithAppliesTo(CouponCreateRequest request) {
        CouponInfo couponInfo = couponInfoRepository.save(request.toEntity());
        CouponTargetType appliesToType = CouponTargetType.fromString(request.getAppliesToType());

        CouponAppliesTo appliesTo = CouponAppliesTo.createWithCouponInfo(
                couponInfo,
                request.getAppliesToId(),
                appliesToType,
                request.getAppliesToName()
        );
        couponAppliesToRepository.save(appliesTo);
        return couponInfo.getId();
    }

    public CouponInfoReadPageResponse getCouponsPage(Pageable pageable) {
        Page<CouponInfo> couponInfoPage = couponInfoRepository.findWithDynamicQuery(pageable);
        return CouponInfoReadPageResponse.fromPage(couponInfoPage);
    }

    public Page<CouponInfo> getCoupons(Pageable pageable) {
        return couponInfoRepository.findWithDynamicQuery(pageable);
    }

    @Transactional
    public Long modifyCouponInfo(CouponModifyRequest request, Long couponInfoId) {
        CouponInfo couponInfo = couponInfoRepository.findById(couponInfoId)
                .orElseThrow(() -> new EntityNotFoundException("CouponInfo not found for id: " + couponInfoId));
        CouponAppliesTo couponAppliesTo = couponAppliesToRepository.findByCouponInfoId(couponInfoId)
                .orElseThrow(() -> new EntityNotFoundException("CouponAppliesTo not found for couponInfoId: " + couponInfoId));

        couponInfo.updateDetails(request);
        couponAppliesTo.updateDetails(request);

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

    public List<CouponInfoItemWithAvailabilityResponse> getActiveCouponsForProductAndCategoryWithAvailability(long memberId,
                                                                                              long productId, long categoryId) {
        List<CouponInfo> couponInfos = couponInfoRepository.findCouponInfosByProductIdAndCategoryId(productId, categoryId);

        Set<Long> downloadedCouponIds = memberCouponRepository.findByMemberId(memberId).stream()
                .map(MemberCoupon::getCouponInfoId)
                .collect(Collectors.toSet());

        return couponInfos.stream()
                .map(couponInfo -> CouponInfoItemWithAvailabilityResponse.from(
                        couponInfo,
                        !downloadedCouponIds.contains(couponInfo.getId()))  // isDownloadable is true if memberId does not have this coupon
                )
                .collect(Collectors.toList());
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


    @Transactional
    public void downloadCoupon(Long memberId, Long couponId) {
        memberCouponRepository.findByMemberIdAndCouponInfoId(memberId, couponId)
            .ifPresent(mc -> {
                throw new ErrorResponseException("이미 쿠폰을 발급한 사용자입니다.");
            });

        String lockKey = couponDownloadLockPrefix + couponId;

        RLock lock = redissonClient.getLock(lockKey);
        try {
            boolean isLocked = lock.tryLock(10, 2, TimeUnit.SECONDS);
            if (!isLocked) {
                // 다른 곳에서 락이 이미 사용 중일 때 발생 ( 2초동안 pub/sub으로 시도하다가 실패하면 예외 던짐 )
                throw new ErrorResponseException("락 획득 실패. 다른 요청이 락을 이미 보유하고 있거나 시스템에 지연이 있을 수 있음. - key: " + lockKey);
            }
            log.info("락 획득 후 로직 발동" + lockKey);
            
            int updatedCount = couponInfoRepository.decreaseRemainingQuantity(couponId);
            if (updatedCount == 0) {
                throw new ErrorResponseException("해당 쿠폰이 모두 소진되었거나 존재하지 않습니다.");
            }
            MemberCoupon memberCoupon = MemberCoupon.builder()
                .memberId(memberId)
                .couponInfoId(couponId)
                .createdAt(LocalDateTime.now())
                .isUsed(false)
                .build();
            memberCouponRepository.save(memberCoupon);
                
        } catch (InterruptedException e) {
            // 다른 쓰레드가 대기 중인 현재 쓰레드를 방해할 때 발생
            Thread.currentThread().interrupt();
            throw new IllegalStateException("대기 중인 락이 인터럽트 됨. 서비스가 종료되거나 재시작되는 등의 상황에서 발생할 수 있음. - key: " + lockKey);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }


    @Transactional
    public MultipleCouponDownloadResponse downloadCoupons(Long memberId, List<Long> couponInfoIds) {
        MultipleCouponDownloadResponse multipleCouponDownloadResponse = MultipleCouponDownloadResponse.builder().build();
        for( Long couponInfoId : couponInfoIds) {
            try {
                downloadCoupon(memberId, couponInfoId);
                multipleCouponDownloadResponse.addSuccess(couponInfoId);
            } catch (Exception e) {
                log.error(e.getMessage());
                multipleCouponDownloadResponse.addFailure(couponInfoId);
            }
        }
        return multipleCouponDownloadResponse;
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

    @Transactional
    public void processCouponUsageAndPublishEvent(OrderDTO orderDTO) {
        Long memberId = orderDTO.getMemberId();
        List<Long> couponIds = orderDTO.getCouponInfos();

        // 날릴 필요 없는 쿼리 최적화
        if (couponIds.isEmpty()) { 
            // chreography라서 날려줘야.
            couponEventProducer.produceCouponSuccessMessage(orderDTO);
            return;
        }

        // 해당 회원의 모든 쿠폰 정보를 한 번의 쿼리로 가져옴.(memberCoupon과 영속화시킨 CouponInfo)
        List<MemberCoupon> memberCoupons = memberCouponRepository.findMemberCouponsByMemberIdAndCouponInfoIds(memberId, couponIds);
        // 가져온 쿠폰의 수가 요청한 쿠폰 수와 동일한지 확인.
        if (couponIds.size() != memberCoupons.size()) {
            throw new EntityNotFoundException("모든 쿠폰 정보를 찾을 수 없습니다.");
        }

        LocalDateTime now = LocalDateTime.now();
        for (MemberCoupon memberCoupon : memberCoupons) {
            if (memberCoupon.getIsUsed()) {
                throw new ErrorResponseException("이미 사용된 쿠폰입니다.");
            }

            CouponInfo couponInfo = memberCoupon.getCouponInfo();
            LocalDateTime expirationTimeWithGracePeriod = couponInfo.getEndAt().plusHours(1);
            // 쿠폰의 유효기간 확인
            if (now.isBefore(couponInfo.getStartAt()) || now.isAfter(expirationTimeWithGracePeriod)) {
                throw new IllegalStateException("쿠폰의 유효기간이 지났습니다.");
            }
            memberCoupon.markAsUsed();
        }
        couponEventProducer.produceCouponSuccessMessage(orderDTO);
    }

    @Transactional
    public void restoreUsedCoupons (Long memberId, List<Long> couponIds) {
        // 날릴 필요 없는 쿼리 최적화
        if (couponIds.isEmpty()) { return; }

        List<MemberCoupon> memberCoupons = memberCouponRepository.findByMemberIdAndCouponInfoIdIn(memberId, couponIds);
        memberCoupons.forEach(MemberCoupon::restore);
    }


    public List<CouponValidationResponse> validateCouponsForOrder(Long memberId, List<CouponValidationRequest> request) {
        List<Long> couponInfoIds = new ArrayList<>();
        for (CouponValidationRequest couponValidationRequest : request) {
            couponInfoIds.add(couponValidationRequest.getCouponInfoId());
        }

        // memberId와 couponInfoIds로 쿠폰 정보를 가져옴 IN연산자라서 내부 최적화로 인해 순서보장안됨.
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
            if (couponInfo == null) {
                throw new ErrorResponseException("연관된 쿠폰 정보가 존재하지 않습니다.");
            }

            if (couponInfo.getEndAt().isBefore(LocalDateTime.now().minusHours(1))) {
                // 사용에 한해서는 1시간의 여유를 더 줌. 조회는 endAt에서 끝.
                throw new ErrorResponseException("사용 유효기간이 지난 쿠폰입니다.");
            }

            CouponTargetType targetType = couponInfo.getAppliesTo().getAppliesToType();
            Long targetId = couponInfo.getAppliesTo().getAppliesToId();
            if ((targetType == CouponTargetType.PRODUCT &&
                    !targetId.equals(couponValidationRequest.getProductId())) ||
                (targetType == CouponTargetType.CATEGORY &&
                    !targetId.equals(couponValidationRequest.getCategoryId()))) {

                String errorMessage = targetType == CouponTargetType.PRODUCT ?
                        "해당 상품에 적용되지 않는 쿠폰정보입니다." :
                        "해당 카테고리에 적용되지 않는 쿠폰정보입니다.";
                throw new ErrorResponseException(errorMessage);
            }

            responses.add(CouponValidationResponse.from(couponValidationRequest.getProductId(), couponInfo));
        }
        return responses;
    }

    public CheckoutCouponApplicationResponse findApplicableCoupons(Long memberId, MultipleProductsCouponRequest request) {
        // memberCoupon의 isUsed 제거, 연결된 couponInfo의 startAt, endAt을 현재시각과 비교해서 active한것들만 가져옴
        List<MemberCoupon> activeNotUsedMemberCoupons = memberCouponRepository.findActiveAndUnusedCouponsByMemberId(memberId);

        // 빈 nested List 생성
        List<List<CouponInfoItemCheckoutResponse>> nestedCouponInfoItemResponses = new ArrayList<>();

        // request객체의 product/category Pair 순회
        for (MultipleProductsCouponRequest.ProductCategoryPair productCategoryPair : request.getProducts()) {
            // 받아온 쿠폰들 중 categoryId, productId 맞는 쿠폰들을 list로 넣음.
            List<CouponInfoItemCheckoutResponse> applicableCoupons = activeNotUsedMemberCoupons.stream()
                    .map(MemberCoupon::getCouponInfo)
                    .filter(couponInfo -> isCouponApplicable(couponInfo, productCategoryPair))
                    .map(CouponInfoItemCheckoutResponse::from)
                    .collect(Collectors.toList());

            nestedCouponInfoItemResponses.add(applicableCoupons);
        }

        return CheckoutCouponApplicationResponse.builder()
                .nestedCouponInfoItemResponses(nestedCouponInfoItemResponses)
                .build();
    }

    public MemberCouponInfoReadPageResponse getMemberCouponsPage(Long memberId, Pageable pageable) {
        Page<MemberCoupon> memberCouponPage = memberCouponRepository.findMemberCouponsWithCouponInfoByMemberId(memberId, pageable);

        List<MemberCouponInfoReadItemResponse> items = memberCouponPage.getContent().stream()
                .map(mc -> MemberCouponInfoReadItemResponse.ofEntities(mc.getCouponInfo(), mc))
                .collect(Collectors.toList());

        return new MemberCouponInfoReadPageResponse(items, memberCouponPage.getTotalElements());
    }

    // TODO: 추상화해서 공통로직 뺴기.
    // validateCouponsForOrder 메소드에도 같은 필터 로직이 있음.(변수 이름 같지만, 객체 타입이 달라서 추상화 필요)
    private boolean isCouponApplicable(CouponInfo couponInfo, MultipleProductsCouponRequest.ProductCategoryPair pair) {
        return (couponInfo.getAppliesTo().getAppliesToType() == CouponTargetType.PRODUCT
                && couponInfo.getAppliesTo().getAppliesToId().equals(pair.getProductId()))
                || (couponInfo.getAppliesTo().getAppliesToType() == CouponTargetType.CATEGORY
                && couponInfo.getAppliesTo().getAppliesToId().equals(pair.getCategoryId()));
    }
}
