package com.dailyon.promotionservice.domain.coupon.service;

import ch.qos.logback.core.net.SyslogOutputStream;
import com.dailyon.promotionservice.common.exceptions.ErrorResponseException;
import com.dailyon.promotionservice.domain.coupon.api.request.CouponCreateRequest;
import com.dailyon.promotionservice.domain.coupon.api.request.CouponModifyRequest;
import com.dailyon.promotionservice.domain.coupon.api.request.MultipleProductsCouponRequest;
import com.dailyon.promotionservice.domain.coupon.entity.*;
import com.dailyon.promotionservice.domain.coupon.entity.enums.CouponTargetType;
import com.dailyon.promotionservice.domain.coupon.entity.enums.DiscountType;
import com.dailyon.promotionservice.domain.coupon.repository.CouponAppliesToRepository;
import com.dailyon.promotionservice.domain.coupon.repository.CouponInfoRepository;
import com.dailyon.promotionservice.domain.coupon.repository.MemberCouponRepository;
import com.dailyon.promotionservice.domain.coupon.service.response.CheckoutCouponApplicationResponse;
import com.dailyon.promotionservice.domain.coupon.service.response.CouponExistenceResponse;
import com.dailyon.promotionservice.domain.coupon.service.response.CouponInfoItemResponse;
import com.dailyon.promotionservice.domain.coupon.service.response.CouponInfoItemWithAvailabilityResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.*;

import static com.dailyon.promotionservice.domain.coupon.entity.enums.DiscountType.fromString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;


@Transactional
@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles(value = {"test"})
public class CouponServiceTest {
    @Autowired EntityManager em;

    @Autowired CouponService couponService;

    @Autowired MemberCouponRepository memberCouponRepository;
    @Autowired CouponInfoRepository couponInfoRepository;
    @Autowired CouponAppliesToRepository couponAppliesToRepository;

    @Autowired RedisTemplate<String, String> redisTemplate;
    @Autowired ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        testCouponInfoDataSetup();
//        testMemberCouponDataSetup();
        System.out.println("@@@@@@@@@@@@@@@SET UP COMPLETE@@@@@@@@@@@@@@@");
    }

    @AfterEach
    void tearDown() {
        System.out.println("@@@@@@@@@@@@@@@ROLLBACK@@@@@@@@@@@@@@@");
        memberCouponRepository.deleteAllInBatch();
        couponAppliesToRepository.deleteAllInBatch();
        couponInfoRepository.deleteAllInBatch();
    }


    @Test
    @DisplayName("[관리자] 쿠폰 생성 - 유효한 요청")
    void createCouponInfoWithAppliesToSuccessfully() {
        // given
        LocalDateTime startTime = LocalDateTime.now().plusDays(1);
        LocalDateTime endTime = LocalDateTime.now().plusDays(10);

        CouponCreateRequest request = new CouponCreateRequest(
                "Summer Sale",
                "PERCENTAGE", // discountRate
                20L, // discountAmount는 controller에서 XOR validation
                startTime,
                endTime,
                1000,
                "PRODUCT",
                1L, // product ID 임의로 넣음.
                "나이키신발",
                true,
                "https://image.url/summer-sale.jpg",
                0L,
                null
        );

        // when
        Long createdCouponId = couponService.createCouponInfoWithAppliesTo(request);

        // then
        CouponInfo couponInfo = em.find(CouponInfo.class, createdCouponId);
        CouponAppliesTo couponAppliesTo = em.find(CouponAppliesTo.class, createdCouponId);

        assertThat(couponInfo).isNotNull();
        assertThat(couponInfo.getName()).isEqualTo(request.getName());
        assertThat(couponInfo.getDiscountType()).isEqualTo(fromString(request.getDiscountType()));
        assertThat(couponInfo.getDiscountValue()).isEqualTo(request.getDiscountValue());
        assertThat(couponInfo.getStartAt()).isEqualTo(startTime);
        assertThat(couponInfo.getEndAt()).isEqualTo(endTime);
        assertThat(couponInfo.getIssuedQuantity()).isEqualTo(request.getIssuedQuantity());

        assertThat(couponAppliesTo).isNotNull();
        assertThat(couponAppliesTo.getCouponInfo()).isEqualTo(couponInfo);
        assertThat(couponAppliesTo.getAppliesToId()).isEqualTo(request.getAppliesToId());
        assertThat(couponAppliesTo.getAppliesToType()).isEqualTo(CouponTargetType.fromString(request.getAppliesToType()));
    }

    @Test
    @DisplayName("[관리자] 쿠폰 생성 - 존재하지 않는 appliesToType string 입력")
    void createCouponInfoWithInvalidAppliesToType_ShouldThrowException() {
        // given
        LocalDateTime startTime = LocalDateTime.now().plusDays(1);
        LocalDateTime endTime = LocalDateTime.now().plusDays(10);

        CouponCreateRequest request = new CouponCreateRequest(
                "Invalid Type Sale",
                "FIXED_AMOUNT",
                5000L,
                startTime,
                endTime,
                1000,
                "INVALID_TYPE", // CouponType ENUM에 존재하는 값일것.
                1L,
                "나이키신발",
                true,
                "https://image.url/invalid-type-sale.jpg",
                0L,
                null
        );

        // when // then
        assertThrows(IllegalArgumentException.class,
                () -> couponService.createCouponInfoWithAppliesTo(request),
                "No enum constant for string value: INVALID_TYPE");
    }

    @Test
    @DisplayName("[관리자] 쿠폰정보 수정 - 유효한 요청")
    void modifyCouponInfoSuccessfully() {
        // given
        Long existingCouponInfoId = createTestCouponInfo();
        CouponModifyRequest modifyRequest = createCouponModifyRequest();

        // when
        Long updatedCouponId = couponService.modifyCouponInfo(modifyRequest, existingCouponInfoId);

        // then
        CouponInfo updatedCouponInfo = em.find(CouponInfo.class, updatedCouponId);

        assertThat(updatedCouponInfo).isNotNull();
        assertThat(updatedCouponInfo.getName()).isEqualTo(modifyRequest.getName());
        assertThat(updatedCouponInfo.getDiscountType()).isEqualTo(fromString(modifyRequest.getDiscountType()));
        assertThat(updatedCouponInfo.getDiscountValue()).isEqualTo(modifyRequest.getDiscountValue());
        assertThat(updatedCouponInfo.getStartAt()).isEqualTo(modifyRequest.getStartAt());
        assertThat(updatedCouponInfo.getEndAt()).isEqualTo(modifyRequest.getEndAt());
        assertThat(updatedCouponInfo.getIssuedQuantity()).isEqualTo(modifyRequest.getIssuedQuantity());
        assertThat(updatedCouponInfo.getRemainingQuantity()).isEqualTo(modifyRequest.getIssuedQuantity());
        assertThat(updatedCouponInfo.getTargetImgUrl()).isEqualTo(modifyRequest.getTargetImgUrl()); // null이 안들어와서 이렇게 진행.
    }

    @Test
    @DisplayName("[관리자] 쿠폰정보 수정 - 존재하지 않는 discountType string 입력")
    void modifyCouponInfoWithInvalidDiscountTypeShouldThrowException() {
        // given
        Long existingCouponInfoId = createTestCouponInfo();
        CouponModifyRequest modifyRequest = createCouponModifyRequestWithInvalidDiscountType();

        // when / then
        assertThatThrownBy(() -> couponService.modifyCouponInfo(modifyRequest, existingCouponInfoId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No enum constant for string value");
    }

    @Test
    @DisplayName("[관리자] 쿠폰정보 수정 - 존재하지 않는 CouponInfo ID로 요청")
    void modifyNonExistentCouponInfoShouldThrowException() {
        // given
        Long nonExistentCouponInfoId = -1L; // Non-existent ID
        CouponModifyRequest modifyRequest = createCouponModifyRequest();

        // when / then
        assertThrows(EntityNotFoundException.class,
                () -> couponService.modifyCouponInfo(modifyRequest, nonExistentCouponInfoId));
    }

    @Test
    @DisplayName("[관리자] 쿠폰 삭제 - 유효한 요청. CouponInfo와 1:1 cascade로 연결된 CouponAppliesTo 삭제 확인")
    void deleteCouponInfoWithAppliesToSuccessfully() {
        // given
        Long couponInfoId = createCouponInfoWithAppliesTo();

        // when
        couponService.deleteCouponInfoWithAppliesTo(couponInfoId);
        em.flush();
        em.clear();

        // then
        assertThat(couponInfoRepository.findById(couponInfoId)).isEmpty();
        assertThat(couponAppliesToRepository.findByCouponInfoId(couponInfoId)).isEmpty(); // 얘를 확인해야되는데 왜...
    }

    @Test
    @DisplayName("[관리자] 쿠폰 삭제 - 존재하지 않는 CouponInfo Id로 요청")
    void deleteNonExistingCouponInfoShouldThrowException() {
        // given
        Long nonExistingCouponInfoId = Long.MAX_VALUE; // Some ID that is guaranteed not to exist.

        // when / then
        assertThatThrownBy(() -> couponService.deleteCouponInfoWithAppliesTo(nonExistingCouponInfoId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("CouponInfo not found with id: " + nonExistingCouponInfoId);
    }



    @Test
    @DisplayName("[관리자] 쿠폰 발급중단 - 유효한 요청.")
    void invalidateCouponSuccessfully() {
        // given
        Long couponInfoId = createCouponInfoWithAppliesTo();

        // when
        couponService.invalidateCoupon(couponInfoId);
        em.flush();
        em.clear();

        // then
        Optional<CouponInfo> optionalCouponInfo = couponInfoRepository.findById(couponInfoId);

        assertTrue(optionalCouponInfo.isPresent(), "CouponInfo should be present");
        CouponInfo couponInfo = optionalCouponInfo.get();
        Integer remainingQuantity = couponInfo.getRemainingQuantity();

        assertThat(remainingQuantity).isEqualTo(0);
    }


    @Test
    @DisplayName("[관리자] 쿠폰 발급중단 - 존재하지 않는 CouponInfo Id로 요청")
    void invalidateNonExistingCouponInfoShouldThrowException() {
        // given
        Long nonExistingCouponInfoId = Long.MAX_VALUE;

        // when / then
        assertThatThrownBy(() -> couponService.invalidateCoupon(nonExistingCouponInfoId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("CouponInfo not found with id: " + nonExistingCouponInfoId);
    }


    @Test
    @DisplayName("Product IDs 기준으로 현재시각 기준 발급가능 쿠폰 존재 확인")
    void checkCouponsExistenceByProductIds() {
        // given
        List<Long> productIds = Arrays.asList(1L, 2L, 3L, 4L);
        Set<Long> productIdsWithCoupons = new HashSet<>(Arrays.asList(1L, 2L));

        // when
        List<CouponExistenceResponse> existenceResponses = couponService.checkCouponsExistenceByProductIds(productIds);

        // then
        assertNotNull(existenceResponses);
        assertEquals(productIds.size(), existenceResponses.size());
        assertThat(existenceResponses).extracting("productId")
                .containsExactlyInAnyOrderElementsOf(productIds);
        assertThat(existenceResponses).extracting("hasAvailableCoupon")
                .containsExactly(true, true, false, false);
    }

    @Test
    @DisplayName("단일 상품에 적용되는 쿠폰 조회 productId and categoryId - 유효한 요청")
    void searchCouponsOnSingleProductByProductIdAndCategoryId() {
        Long productId = 1L;
        Long categoryId = 1L;
        List<CouponInfoItemResponse> expectedCoupons = List.of(
                new CouponInfoItemResponse(1L, CouponTargetType.PRODUCT, 1L, DiscountType.PERCENTAGE, 20L, LocalDateTime.parse("2023-12-04T16:53:35.066194"), 0L, null),
                new CouponInfoItemResponse(1L, CouponTargetType.CATEGORY, 1L, DiscountType.PERCENTAGE, 10L, LocalDateTime.parse("2023-12-04T16:53:35.066194"), 0L, null)
        );

        List<CouponInfoItemResponse> activeCoupons = couponService.getActiveCouponsForProductAndCategory(productId, categoryId);
        assertEquals(expectedCoupons.size(), activeCoupons.size(), "The list of active coupons does not match the expected size");
    }

    @Test
    @DisplayName("쿠폰 다운로드 - 유효한 요청")
    void downloadCoupon_Success() {
        // given
        Long memberId = 1L;
        Long couponInfoId = normalCategoryDataSetup();
        Optional<CouponInfo> optionalCouponInfo = couponInfoRepository.findById(couponInfoId);
//        System.out.println("최초 조회개수: " + optionalCouponInfo.get().getRemainingQuantity().toString());

        assertTrue(optionalCouponInfo.isPresent());
        CouponInfo couponInfo = optionalCouponInfo.get();
        int initialRemainingQuantity = couponInfo.getRemainingQuantity();

        // when
        couponService.downloadCoupon(memberId, couponInfoId);

        Optional<CouponInfo> resCouponInfo = couponInfoRepository.findById(couponInfoId);
        int remainingQuantity = resCouponInfo.get().getRemainingQuantity();

        // then
        assertEquals(initialRemainingQuantity - 1, remainingQuantity);
    }

    @Test
    @DisplayName("쿠폰 다운로드 - 쿠폰의 endAt이 만료된 경우")
    void downloadCoupon_ExpiredCoupon() {
        // given
        Long memberId = 1L;
        Long couponInfoId = expiredCategoryDataSetup();
        Optional<CouponInfo> optionalCouponInfo = couponInfoRepository.findById(couponInfoId);
        assertTrue(optionalCouponInfo.isPresent());


        // when
        Exception exception = assertThrows(ErrorResponseException.class, () -> {
            couponService.downloadCoupon(memberId, couponInfoId);
        });

        // then
        assertEquals("해당 쿠폰 이벤트는 만료된 이벤트입니다.", exception.getMessage());
    }

    @Test
    @DisplayName("쿠폰 다운로드 - 쿠폰의 남은 수량이 0개인 경우")
    void downloadCoupon_NoRemainingQuantity() {
        // given
        Long memberId = 1L;
        Long couponInfoId = noRemainingCouponDataSetup();
        Optional<CouponInfo> optionalCouponInfo = couponInfoRepository.findById(couponInfoId);
        assertTrue(optionalCouponInfo.isPresent());

        // when
        Exception exception = assertThrows(ErrorResponseException.class, () -> {
            couponService.downloadCoupon(memberId, couponInfoId);
        });

        // then
        assertEquals("해당 쿠폰이 모두 소진되었거나 존재하지 않습니다.", exception.getMessage());
    }


    @Test
    @DisplayName("유저의 단일 상품 적용 다운로드 가능/기 다운로드 쿠폰 리스트 조회 - 유효한요청/쿠폰 없음")
    void whenGetActiveCouponsForProductAndCategoryWithAvailability_thenSuccessWithEmpty() {
        // given
        long memberId = 1L;
        long productId = 9999L; // 존재하지않는 상품 id
        long categoryId = 9999L; // 존재하지 않는 카테고리 id

        // when
        List<CouponInfoItemWithAvailabilityResponse> results = couponService.getActiveCouponsForProductAndCategoryWithAvailability(memberId, productId, categoryId);

        // then
        assertTrue(results.isEmpty());
    }

    @Test
    @DisplayName("유저의 단일 상품 적용 다운로드 가능/기 다운로드 쿠폰 리스트 조회 - 유효한요청/모두 다운로드 가능한 상황")
    void whenGetActiveCouponsForProductAndCategoryWithAvailability_thenSuccess() {
        // given
        long memberId = 1L;
        long productId = 1L;
        long categoryId = 1L;

        // when
        List<CouponInfoItemWithAvailabilityResponse> results = couponService.getActiveCouponsForProductAndCategoryWithAvailability(memberId, productId, categoryId);

        // then
        assertFalse(results.isEmpty());
        assertTrue(results.stream().anyMatch(CouponInfoItemWithAvailabilityResponse::getIsDownloadable));

    }

    @Test
    @DisplayName("유저의 단일 상품 적용 다운로드 가능/기 다운로드 쿠폰 리스트 조회 - 다운로드 이후 달라지는 결과 확인")
    void whenGetActiveCouponsForProductAndCategoryWithAvailability_CompareAfterDownloadCoupon() {
        // given
        long memberId = 1L;
        long productId = 1L;
        long categoryId = 1L;

        // when
        List<CouponInfoItemWithAvailabilityResponse> resultsWithBeforeDownload = couponService.getActiveCouponsForProductAndCategoryWithAvailability(memberId, productId, categoryId);
        couponService.downloadCoupon(memberId, resultsWithBeforeDownload.get(0).getCouponInfoId()); // 다운로드 해서 달라지는걸 확인

        List<CouponInfoItemWithAvailabilityResponse> resultsWithAfterDownload = couponService.getActiveCouponsForProductAndCategoryWithAvailability(memberId, productId, categoryId);

        // then
        assertNotEquals(resultsWithBeforeDownload, resultsWithAfterDownload);
    }

    private Long createTestCouponInfo() {
        // 실제 테스트 데이터로 'CouponInfo' 인스턴스 생성
        CouponInfo testCouponInfo = CouponInfo.builder()
                .name("Test Coupon")
                .discountType(fromString("FIXED_AMOUNT"))
                .discountValue(1000L)
                .startAt(LocalDateTime.now().plusDays(1))
                .endAt(LocalDateTime.now().plusDays(10))
                .issuedQuantity(1000)
                .remainingQuantity(1000)
                .targetImgUrl("https://image.url/test-coupon.jpg")
                .build();

        CouponAppliesTo testCouponAppliesTo = CouponAppliesTo.builder()
                .couponInfo(testCouponInfo) // 1:1 식별관계라서 직접 couponInfoId를 할당하려 하면 오류
                .appliesToId(1L)
                .appliesToType(CouponTargetType.PRODUCT)
                .appliesToName("나이키신발")
                .build();


        // 엔티티를 영속성 컨텍스트에 저장하고 flush (commit은 테스트 프레임워크가 관리)
        em.persist(testCouponInfo);
        em.persist(testCouponAppliesTo);
        em.flush();

        // 생성된 쿠폰 정보의 ID 반환
        return testCouponInfo.getId();
    }

    @Test
    @DisplayName("Checkout에 적용 가능한 쿠폰 리스트 반환 - 멤버 및 상품 별")
    void whenFindApplicableCoupons_thenReturnNestedCouponInfoList() {
        // Given
        Long memberId = 1L;
        LocalDateTime now = LocalDateTime.now();

        CouponInfo couponInfo = CouponInfo.builder()
                .name("할인 쿠폰")
                .discountType(DiscountType.FIXED_AMOUNT)
                .discountValue(5000L)
                .startAt(now.minusDays(1))
                .endAt(now.plusDays(10))
                .issuedQuantity(100)
                .remainingQuantity(100)
                .minPurchaseAmount(0L)
                .build();
        couponInfo = couponInfoRepository.save(couponInfo);

        MemberCoupon memberCoupon = MemberCoupon.builder()
                .memberId(memberId)
                .couponInfoId(couponInfo.getId())
                .couponInfo(couponInfo)
                .isUsed(false)
                .build();
        memberCoupon = memberCouponRepository.save(memberCoupon);


        MultipleProductsCouponRequest request = MultipleProductsCouponRequest.builder()
                .products(
                        List.of(
                                new MultipleProductsCouponRequest.ProductCategoryPair(1L, 1L),
                                new MultipleProductsCouponRequest.ProductCategoryPair(2L, 10L),
                                new MultipleProductsCouponRequest.ProductCategoryPair(3L, 1L)
                        )
                )
                .build();

        // When
        CheckoutCouponApplicationResponse result = couponService.findApplicableCoupons(memberId, request);

        // Then
        assertFalse(result.getNestedCouponInfoItemResponses().isEmpty());
    }

    private CouponModifyRequest createCouponModifyRequest() {
        // CouponModifyRequest에 필요한 데이터를 기반으로 객체 생성
        return CouponModifyRequest.builder()
                .name("Updated Coupon Name") // name 변경값
                .discountType("PERCENTAGE")
                .discountValue(19L)
                .startAt(LocalDateTime.now().plusDays(3)) // 1 -> 3
                .endAt(LocalDateTime.now().plusDays(20)) // 10 -> 20
                .issuedQuantity(500) // 1000 -> 500
                .requiresConcurrencyControl(false)
                .appliesToType("PRODUCT")
                .appliesToId(1L)
                .targetImgUrl("https://image.url/updated-coupon.jpg") // url 변경값
                .build();
    }

    private CouponModifyRequest createCouponModifyRequestWithInvalidDiscountType() {
        // 유효하지 않은 discountType을 가진 CouponModifyRequest 객체
        return CouponModifyRequest.builder()
                .name("Updated Coupon Name")
                .discountType("INVALID_DISCOUNT_TYPE")
                .discountValue(19L)
                .startAt(LocalDateTime.now().plusDays(3))
                .endAt(LocalDateTime.now().plusDays(20))
                .appliesToId(1L)
                .issuedQuantity(500)
                .requiresConcurrencyControl(false)
                .targetImgUrl("https://image.url/updated-coupon.jpg")
                .build();
    }

    private Long createCouponInfoWithAppliesTo() {
        // 위에 테스트용으로 만든 메소드 사용해서 환경 구축
//        Long couponInfoId = createTestCouponInfo();

        CouponInfo testCouponInfo = CouponInfo.builder()
                .name("Test Coupon")
                .discountType(fromString("FIXED_AMOUNT"))
                .discountValue(1000L)
                .startAt(LocalDateTime.now().plusDays(1))
                .endAt(LocalDateTime.now().plusDays(10))
                .issuedQuantity(1000)
                .remainingQuantity(1000)
                .targetImgUrl("https://image.url/test-coupon.jpg")
                .build();

        CouponTargetType appliesToType = CouponTargetType.PRODUCT;
        Long appliesToId = 1L;
        String appliesToName = "나이키신발";

        CouponAppliesTo testCouponAppliesTo = CouponAppliesTo.createWithCouponInfo(testCouponInfo, appliesToId, appliesToType, appliesToName);
        couponAppliesToRepository.save(testCouponAppliesTo);
        em.flush();
        em.clear();

        return testCouponInfo.getId();
    }

    private void testCouponInfoDataSetup() {
            // PRODUCT1, PERCENTAGE
            CouponCreateRequest create_request1 = new CouponCreateRequest(
                    "PERCENTAGE_PRODUCT_SALE",
                    "PERCENTAGE",
                    20L,
                    LocalDateTime.now().minusDays(1),
                    LocalDateTime.now().plusDays(5),
                    1000,
                    "PRODUCT",
                    1L, // product ID 임의로 넣음.
                    "나이키신발",
                    true,
                    "https://image.url/summer-sale.jpg",
                0L,
                null
            );

        // PRODUCT2, FIXED_AMOUNT
            CouponCreateRequest create_request2 = new CouponCreateRequest(
                    "FIXED_AMOUNT_PRODUCT_SALE",
                    "FIXED_AMOUNT",
                    5000L,
                    LocalDateTime.now().minusDays(1),
                    LocalDateTime.now().plusDays(5),
                    1000,
                    "PRODUCT",
                    2L, // product ID 임의로 넣음.
                    "나이키신발",
                    true,
                    "https://image.url/summer-sale.jpg",
                0L,
                null
            );

        // CATEGORY1, PERCENTAGE
            CouponCreateRequest create_request3 = new CouponCreateRequest(
                    "PERCENTAGE_CATEGORY_SALE",
                    "PERCENTAGE", // discountRate
                    10L,
                    LocalDateTime.now().minusDays(1),
                    LocalDateTime.now().plusDays(5),
                    1000,
                    "CATEGORY",
                    1L, // product ID 임의로 넣음.
                    "나이키신발",
                    true,
                    "https://image.url/summer-sale.jpg",
                    0L,
                    null
            );

        // CATEGORY2, FIXED_AMOUNT
            CouponCreateRequest create_request4 = new CouponCreateRequest(
                    "FIXED_AMOUNT_CATEGORY_SALE",
                    "FIXED_AMOUNT", // discountRate
                    20L,
                    LocalDateTime.now().minusDays(1),
                    LocalDateTime.now().plusDays(5),
                    1000,
                    "CATEGORY",
                    2L, // product ID 임의로 넣음.
                    "나이키신발",
                    true,
                    "https://image.url/summer-sale.jpg",
                    0L,
                    null
            );


        couponService.createCouponInfoWithAppliesTo(create_request1);
        couponService.createCouponInfoWithAppliesTo(create_request2);
        couponService.createCouponInfoWithAppliesTo(create_request3);
        couponService.createCouponInfoWithAppliesTo(create_request4);

        em.flush();
        em.clear();
    }

    private Long normalCategoryDataSetup() {
        CouponCreateRequest normalCase1 = new CouponCreateRequest(
                "PERCENTAGE_CATEGORY_SALE",
                "PERCENTAGE", // discountRate
                10L,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(5),
                1000,
                "CATEGORY",
                1L, // category ID 임의로 넣음.
                "신발카테고리",
                false,
                "https://image.url/summer-sale.jpg",
                0L,
                null
        );
        Long couponInfoId = couponService.createCouponInfoWithAppliesTo(normalCase1);
        em.flush();
        em.clear();
        return couponInfoId;
    }

    private Long normalProductDataSetup() {
        CouponCreateRequest normalCase1 = new CouponCreateRequest(
                "PERCENTAGE_CATEGORY_SALE",
                "PERCENTAGE", // discountRate
                10L,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(5),
                1000,
                "PRODUCT",
                1L, // category ID 임의로 넣음.
                "나이키신발",
                false,
                "https://image.url/summer-sale.jpg",
                0L,
                null
        );
        Long couponInfoId = couponService.createCouponInfoWithAppliesTo(normalCase1);
        em.flush();
        em.clear();
        return couponInfoId;
    }

    private Long expiredCategoryDataSetup() {
        CouponCreateRequest normalCase1 = new CouponCreateRequest(
                "PERCENTAGE_CATEGORY_SALE",
                "PERCENTAGE", // discountRate
                10L,
                LocalDateTime.now().minusDays(2),
                LocalDateTime.now().minusDays(1), // expired
                1000,
                "CATEGORY",
                1L, // category ID 임의로 넣음.
                "신발카테고리",
                false,
                "https://image.url/summer-sale.jpg",
                0L,
                null
        );
        Long couponInfoId = couponService.createCouponInfoWithAppliesTo(normalCase1);
        em.flush();
        em.clear();
        return couponInfoId;
    }

    private Long noRemainingCouponDataSetup() {
        CouponCreateRequest normalCase1 = new CouponCreateRequest(
                "PERCENTAGE_CATEGORY_SALE",
                "PERCENTAGE", // discountRate
                10L,
                LocalDateTime.now().minusDays(2),
                LocalDateTime.now().plusDays(5),
                0,
                "CATEGORY",
                1L, // category ID 임의로 넣음.
                "나이키신발",
                false,
                "https://image.url/summer-sale.jpg",
                0L,
                null
        );
        Long couponInfoId = couponService.createCouponInfoWithAppliesTo(normalCase1);
        em.flush();
        em.clear();
        return couponInfoId;
    }

//    private void testMemberCouponDataSetup() {
//        Optional<CouponInfo> couponInfo1 = couponInfoRepository.findById(1L);
//        Optional<CouponInfo> couponInfo2 = couponInfoRepository.findById(2L);

//        MemberCoupon memberCoupon1 = MemberCoupon.builder()
//                .memberId(5L)
//                .couponInfoId(1L)
//                .isUsed(false)
//                .couponInfo(couponInfo1.get())
//                .build();
//
//        MemberCoupon memberCoupon2 = MemberCoupon.builder()
//                .memberId(5L)
//                .couponInfoId(2L)
//                .isUsed(false)
//                .couponInfo(couponInfo2.get())
//                .build();
//        couponInfo1.ifPresent(info1 -> {
//            MemberCoupon memberCoupon1 = MemberCoupon.builder()
//                    .memberId(5L)
//                    .couponInfoId(info1.getId())
//                    .isUsed(false)
//                    .couponInfo(info1)
//                    .build();
//            memberCouponRepository.save(memberCoupon1);
//        });
//
//        couponInfo2.ifPresent(info2 -> {
//            MemberCoupon memberCoupon2 = MemberCoupon.builder()
//                    .memberId(5L)
//                    .couponInfoId(info2.getId())
//                    .isUsed(false)
//                    .couponInfo(info2)
//                    .build();
//            memberCouponRepository.save(memberCoupon2);
//        });
//

//        memberCouponRepository.save(memberCoupon1);
//        memberCouponRepository.save(memberCoupon2);

//        em.flush();
//        em.clear();
//    }

//    private Long expiredCouponDataSetup() {
//        CouponCreateRequest normalCase1 = new CouponCreateRequest(
//                "PERCENTAGE_CATEGORY_SALE",
//                "PERCENTAGE", // discountRate
//                10L,
//                LocalDateTime.now().minusDays(2),
//                LocalDateTime.now().minusDays(1), // expired
//                1000,
//                "CATEGORY",
//                1L, // category ID 임의로 넣음.
//                false,
//                "https://image.url/summer-sale.jpg",
//                0L,
//                null
//        );
//        return couponService.createCouponInfoWithAppliesTo(normalCase1); // flush clear필요?
//    }




}
