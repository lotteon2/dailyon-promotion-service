package com.dailyon.promotionservice.domain.coupon.service;

import com.dailyon.promotionservice.domain.coupon.api.request.CouponCreateRequest;
import com.dailyon.promotionservice.domain.coupon.entity.CouponAppliesTo;
import com.dailyon.promotionservice.domain.coupon.entity.CouponInfo;
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
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

import org.springframework.test.util.ReflectionTestUtils;

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
        couponInfoRepository.deleteAllInBatch();
        couponAppliesToRepository.deleteAllInBatch();
        memberCouponRepository.deleteAllInBatch();
    }


    @Test
    @DisplayName("쿠폰 정보를 입력 받아 쿠폰을 생성한다.")
    void createCouponInfoWithAppliesToSuccessfully() {
        // given
        CouponCreateRequest request = CouponCreateRequest.builder()
                .name("Summer Sale")
                .discountAmount(1000L) // discountRate는 null
                .startAt(LocalDateTime.now().plusDays(1))
                .endAt(LocalDateTime.now().plusDays(30))
                .issuedQuantity(500)
                .appliesToType("PRODUCT")
                .appliesToId(1L)
                .requiresConcurrencyControl(false)
                .targetImgUrl("http://example.com/coupon.jpg")
                .build();
        CouponInfo couponInfoFromRequest = request.toEntity();

        // simulate the repository behavior
        when(couponInfoRepository.save(any(CouponInfo.class))).thenAnswer(invocation -> {
            CouponInfo arg = invocation.getArgument(0);
            ReflectionTestUtils.setField(arg, "id", 1L); // Use reflection to set the ID
            return arg;
        });

        // when
        Long couponId = couponService.createCouponInfoWithAppliesTo(request);

        // then
        assertThat(couponId).isEqualTo(1L);
        verify(couponInfoRepository).save(refEq(couponInfoFromRequest, "discountRate")); //discountRate - null 무시
        verify(couponAppliesToRepository).save(any(CouponAppliesTo.class));
    }

    @Test
    @DisplayName("할인율과 할인액을 동시에 설정하면 예외가 발생한다.")
    void createCouponInfoWithInvalidDiscount() {
        // given
        CouponCreateRequest request = CouponCreateRequest.builder()
                .name("Summer Sale")
                .discountRate(10) // Discount rate provided
                .discountAmount(1000L) // Discount amount also provided, which is invalid
                .startAt(LocalDateTime.now().plusDays(1))
                .endAt(LocalDateTime.now().plusDays(30))
                .issuedQuantity(500)
                .appliesToType("PRODUCT")
                .appliesToId(1L)
                .requiresConcurrencyControl(true)
                .targetImgUrl("http://example.com/coupon.jpg")
                .build();

        // when // then
        assertThatThrownBy(() -> couponService.createCouponInfoWithAppliesTo(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid discount: either rate or amount must be set, not both");
    }

    @Test
    @DisplayName("할인율과 할인액 둘 다 설정되지 않은 경우 예외가 발생한다.")
    void createCouponInfoWithNoDiscountProvided() {
        // given
        CouponCreateRequest request = CouponCreateRequest.builder()
                .name("Summer Sale")
                // Both discountRate and discountAmount are not set. XOR logic
                .startAt(LocalDateTime.now().plusDays(1))
                .endAt(LocalDateTime.now().plusDays(30))
                .issuedQuantity(500)
                .appliesToType("PRODUCT")
                .appliesToId(1L)
                .requiresConcurrencyControl(true)
                .targetImgUrl("http://example.com/coupon.jpg")
                .build();

        // when // then
        assertThatThrownBy(() -> couponService.createCouponInfoWithAppliesTo(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid discount: either rate or amount must be set, not both nor none");
    }
}
