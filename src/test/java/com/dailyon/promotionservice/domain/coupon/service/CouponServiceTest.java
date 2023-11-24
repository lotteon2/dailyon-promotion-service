package com.dailyon.promotionservice.domain.coupon.service;

import com.dailyon.promotionservice.domain.coupon.api.request.CouponCreateRequest;
import com.dailyon.promotionservice.domain.coupon.entity.CouponAppliesTo;
import com.dailyon.promotionservice.domain.coupon.entity.CouponInfo;
import com.dailyon.promotionservice.domain.coupon.entity.CouponType;
import com.dailyon.promotionservice.domain.coupon.repository.CouponAppliesToRepository;
import com.dailyon.promotionservice.domain.coupon.repository.CouponInfoRepository;
import com.dailyon.promotionservice.domain.coupon.repository.MemberCouponRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
public class CouponServiceTest {
    @Autowired EntityManager em;
    @Autowired CouponService couponService;
    @Autowired CouponInfoRepository couponInfoRepository;
    @Autowired CouponAppliesToRepository couponAppliesToRepository;
    @Autowired MemberCouponRepository memberCouponRepository;

//    @Autowired RedisTemplate<String, String> redisTemplate;
    @Autowired ObjectMapper objectMapper;

    @AfterEach
    void tearDown() {
        couponAppliesToRepository.deleteAllInBatch();
        couponInfoRepository.deleteAllInBatch();
        memberCouponRepository.deleteAllInBatch();
    }


    @Test
    @DisplayName("쿠폰 정보를 입력 받아 쿠폰을 생성한다.")
    void createCouponInfoWithAppliesToSuccessfully() {
        // given
        LocalDateTime startTime = LocalDateTime.now().plusDays(1);
        LocalDateTime endTime = LocalDateTime.now().plusDays(10);

        CouponCreateRequest request = new CouponCreateRequest(
                "Summer Sale",
                20, // discountRate
                null, // discountAmount는 controller에서 XOR validation
                startTime,
                endTime,
                1000,
                "PRODUCT",
                1L, // product ID 임의로 넣음.
                true,
                "https://image.url/summer-sale.jpg"
        );

        // when
        Long createdCouponId = couponService.createCouponInfoWithAppliesTo(request);

        // then
        CouponInfo couponInfo = em.find(CouponInfo.class, createdCouponId);
        CouponAppliesTo couponAppliesTo = em.find(CouponAppliesTo.class, createdCouponId);

        assertThat(couponInfo).isNotNull();
        assertThat(couponInfo.getName()).isEqualTo(request.getName());
        assertThat(couponInfo.getDiscountRate()).isEqualTo(request.getDiscountRate());
        assertThat(couponInfo.getStartAt()).isEqualTo(startTime);
        assertThat(couponInfo.getEndAt()).isEqualTo(endTime);
        assertThat(couponInfo.getIssuedQuantity()).isEqualTo(request.getIssuedQuantity());

        assertThat(couponAppliesTo).isNotNull();
        assertThat(couponAppliesTo.getCouponInfo()).isEqualTo(couponInfo);
        assertThat(couponAppliesTo.getAppliesToId()).isEqualTo(request.getAppliesToId());
        assertThat(couponAppliesTo.getAppliesToType()).isEqualTo(CouponType.fromString(request.getAppliesToType()));
    }

    @Test
    @DisplayName("CouponInfo 생성시 존재하지 않는 appliesToType을 입력하면 예외가 발생한다.")
    void createCouponInfoWithInvalidAppliesToType_ShouldThrowException() {
        // given
        LocalDateTime startTime = LocalDateTime.now().plusDays(1);
        LocalDateTime endTime = LocalDateTime.now().plusDays(10);

        CouponCreateRequest request = new CouponCreateRequest(
                "Invalid Type Sale",
                null,
                5000L,
                startTime,
                endTime,
                1000,
                "INVALID_TYPE", // CouponType ENUM에 존재하는 값일것.
                1L,
                true,
                "https://image.url/invalid-type-sale.jpg"
        );

        // when // then
        assertThrows(IllegalArgumentException.class,
                () -> couponService.createCouponInfoWithAppliesTo(request),
                "No enum constant for string value: INVALID_TYPE");
    }

}
