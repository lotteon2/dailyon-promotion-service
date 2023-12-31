package com.dailyon.promotionservice.domain.coupon.api;

import com.dailyon.promotionservice.ControllerTestSupport;
import com.dailyon.promotionservice.domain.coupon.api.request.CouponCreateRequest;
import com.dailyon.promotionservice.domain.coupon.api.request.CouponModifyRequest;
import com.dailyon.promotionservice.domain.coupon.entity.enums.CouponTargetType;
import com.dailyon.promotionservice.domain.coupon.entity.enums.DiscountType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class CouponAdminControllerTest extends ControllerTestSupport {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;


    @DisplayName("[관리자] 쿠폰 생성 - 유효한 요청")
    @Test
    void createCouponInfoWithValidRequest() throws Exception {
        //given
        CouponCreateRequest request = CouponCreateRequest.builder()
                .name("New Year Sale")
                .discountType(DiscountType.FIXED_AMOUNT.name()) // Changed to use DiscountType enum
                .discountValue(5000L) // Assuming this is a valid fixed amount
                .startAt(LocalDateTime.now().plusDays(1))
                .endAt(LocalDateTime.now().plusDays(10))
                .issuedQuantity(100)
                .appliesToType(CouponTargetType.PRODUCT.name())
                .appliesToId(1L)
                .requiresConcurrencyControl(true)
                .targetImgUrl("https://image.url/target.jpg")
                .build();

        // when // then
        mockMvc
                .perform(
                        post("/admin/coupons")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isNumber())
                .andExpect(jsonPath("$").value(Matchers.greaterThan(-1)));
    }


    @Test
    @DisplayName("[관리자] 쿠폰 생성 - 시작일 없는 요청")
    void createCouponInfoWithNullStartAt() throws Exception {
        // Given
        CouponCreateRequest request = CouponCreateRequest.builder()
                .name("New Year Sale")
                .discountType(DiscountType.FIXED_AMOUNT.name()) // Changed to use DiscountType enum
                .discountValue(5000L) // Assuming this is a valid fixed amount
                .startAt(null)
                .endAt(LocalDateTime.now().plusDays(10))
                .issuedQuantity(100)
                .appliesToType(CouponTargetType.PRODUCT.name())
                .appliesToId(1L)
                .requiresConcurrencyControl(true)
                .targetImgUrl("https://image.url/target.jpg")
                .build();

        // When // Then
        mockMvc
                .perform(
                        post("/admin/coupons")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0]").value("startAt: must not be null"));
    }


    @Test
    @DisplayName("[관리자] 쿠폰 생성 - startAt이 endAt보다 늦은 요청")
    void createCouponInfoWithInvalidDateRange() throws Exception {
        // Given
        LocalDateTime startDateTime = LocalDateTime.now().plusDays(10);
        LocalDateTime endDateTime = LocalDateTime.now().plusDays(1);
        CouponCreateRequest request = CouponCreateRequest.builder()
                .name("New Year Sale")
                .discountType(DiscountType.FIXED_AMOUNT.name()) // Changed to use DiscountType enum
                .discountValue(5000L) // Assuming this is a valid fixed amount
                .startAt(startDateTime)
                .endAt(endDateTime)
                .issuedQuantity(100)
                .appliesToType(CouponTargetType.PRODUCT.name())
                .appliesToId(1L)
                .requiresConcurrencyControl(true)
                .targetImgUrl("https://image.url/target.jpg")
                .build();

        // When // Then
        mockMvc
                .perform(
                        post("/admin/coupons")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.TEXT_PLAIN_VALUE + ";charset=UTF-8"))
                .andExpect(content().string("The start date must be before the end date"));
    }


    @DisplayName("[관리자] 쿠폰 생성 - FIXED_AMOUNT의 discountValue가 음수")
    @Test
    void createCouponInfoWithFixedAmountDiscountValueNegative() throws Exception {
        // Given
        CouponCreateRequest request = CouponCreateRequest.builder()
                .name("Summer Sale")
                .discountType(DiscountType.FIXED_AMOUNT.name())
                .discountValue(-1L)
                .startAt(LocalDateTime.now().plusDays(1))
                .endAt(LocalDateTime.now().plusDays(30))
                .issuedQuantity(100)
                .appliesToType(CouponTargetType.PRODUCT.name())
                .appliesToId(1L)
                .requiresConcurrencyControl(true)
                .targetImgUrl("https://image.url/target.jpg")
                .build();

        // When // Then
        mockMvc
                .perform(
                        post("/admin/coupons")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.TEXT_PLAIN_VALUE + ";charset=UTF-8"))
                .andExpect(content().string("Fixed amount discount value must be non-negative."));
    }


    @DisplayName("[관리자] 쿠폰 생성 - PERCENTAGE의 discountValue가 0 ~ 100 범위를 넘어감")
    @Test
    void createCouponInfoWithPercentageDiscountValueTooHigh() throws Exception {
        // Given
        CouponCreateRequest request = CouponCreateRequest.builder()
                .name("Winter Sale")
                .discountType(DiscountType.PERCENTAGE.name())
                .discountValue(150L) // Percentage over 100
                .startAt(LocalDateTime.now().plusDays(1))
                .endAt(LocalDateTime.now().plusDays(30))
                .issuedQuantity(100)
                .appliesToType(CouponTargetType.PRODUCT.name())
                .appliesToId(1L)
                .requiresConcurrencyControl(true)
                .targetImgUrl("https://image.url/special.jpg")
                .build();

        // When // Then
        mockMvc
                .perform(
                        post("/admin/coupons")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.TEXT_PLAIN_VALUE + ";charset=UTF-8"))
                .andExpect(content().string("Percentage discount value must be between 0 and 100."));
    }


    @DisplayName("[관리자] 쿠폰정보 수정 - 유효한 요청")
    @Test
    void modifyCouponInfoWithValidRequest() throws Exception {
        // Given
        Long couponIdToBeUpdated = 3L;
        CouponModifyRequest request = CouponModifyRequest.builder()
                .name("Updated Name")
                .discountType("PERCENTAGE")
                .discountValue(15L) // Updated discount rate within valid range
                .startAt(LocalDateTime.now().minusDays(10))
                .endAt(LocalDateTime.now().plusDays(20))
                .issuedQuantity(500) // Updated issued quantity
                .requiresConcurrencyControl(false)
                .appliesToType(CouponTargetType.PRODUCT.toString())
                .appliesToId(1L)
                .targetImgUrl("https://image.url/updated.jpg")
                .build();

        assertNull(request.getInvalidDiscountMessage(), "Discount information is not valid");
        assertTrue(request.isValidDateRange(), "Date range is not valid");

        String requestJson = objectMapper.writeValueAsString(request);

        // Stubbing CouponService to return the expected ID on modification // 넣은값이 그대로 나올 수 있도록.
        // 이 설정을 안해주면 mockbean은 무조건 0을 뱉는다.
        when(couponService.modifyCouponInfo(any(CouponModifyRequest.class), eq(couponIdToBeUpdated)))
                .thenReturn(couponIdToBeUpdated);

        // When // Then
        mockMvc.perform(
                        patch("/admin/coupons/{id}", couponIdToBeUpdated)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").value(couponIdToBeUpdated));
    }

    @Test
    @DisplayName("[관리자] 쿠폰정보 수정 - 시작일 없는 요청: not null 대표")
    void modifyCouponInfoWithNullStartAt() throws Exception {
        // Given
        Long couponIdToBeUpdated = 3L;
        CouponModifyRequest request = CouponModifyRequest.builder()
                .name("Updated Name")
                .discountType(DiscountType.PERCENTAGE.name())
                .discountValue(15L)
                .startAt(null)
                .endAt(LocalDateTime.now().plusDays(20))
                .issuedQuantity(500)
                .requiresConcurrencyControl(false)
                .appliesToType(CouponTargetType.PRODUCT.toString())
                .appliesToId(1L)
                .targetImgUrl("https://image.url/updated.jpg")
                .build();

        String requestJson = objectMapper.writeValueAsString(request);

        // When // Then
        mockMvc.perform(
                        patch("/admin/coupons/{id}", couponIdToBeUpdated)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson)
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0]").value("startAt: must not be null"));
    }

    @Test
    @DisplayName("[관리자] 쿠폰정보 수정 - startAt이 endAt보다 늦은 요청")
    void modifyCouponInfoWithInvalidDateRange() throws Exception {
        // Given
        Long couponIdToBeUpdated = 4L;
        CouponModifyRequest request = CouponModifyRequest.builder()
                .name("Winter Sale")
                .discountType(DiscountType.FIXED_AMOUNT.name())
                .discountValue(1000L)
                .startAt(LocalDateTime.now().plusDays(10))
                .endAt(LocalDateTime.now().plusDays(5))
                .issuedQuantity(50)
                .requiresConcurrencyControl(false)
                .appliesToType(CouponTargetType.PRODUCT.toString())
                .appliesToId(1L)
                .targetImgUrl("https://image.url/winter.jpg")
                .build();

        String requestJson = objectMapper.writeValueAsString(request);

        // When // Then
        mockMvc.perform(
                        patch("/admin/coupons/{id}", couponIdToBeUpdated)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson)
                )
                .andDo(print())
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.TEXT_PLAIN_VALUE + ";charset=UTF-8"))
                .andExpect(content().string("The start date must be before the end date"));
    }

    @Test
    @DisplayName("[관리자] 쿠폰정보 수정 - FIXED_AMOUNT의 discountValue가 음수")
    void modifyCouponInfoWithFixedAmountDiscountValueNegative() throws Exception {
        // Given
        Long couponIdToBeUpdated = 4L;
        CouponModifyRequest request = CouponModifyRequest.builder()
                .name("Winter Sale")
                .discountType(DiscountType.FIXED_AMOUNT.name())
                .discountValue(-500L) // Negative amount
                .startAt(LocalDateTime.now().plusDays(1))
                .endAt(LocalDateTime.now().plusDays(30))
                .issuedQuantity(50)
                .requiresConcurrencyControl(false)
                .appliesToType(CouponTargetType.PRODUCT.toString())
                .appliesToId(1L)
                .targetImgUrl("https://image.url/winter.jpg")
                .build();

        String requestJson = objectMapper.writeValueAsString(request);

        // When // Then
        mockMvc.perform(
                        patch("/admin/coupons/{id}", couponIdToBeUpdated)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson)
                )
                .andDo(print())
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.TEXT_PLAIN_VALUE + ";charset=UTF-8"))
                .andExpect(content().string("Fixed amount discount value must be non-negative."));
    }

    @Test
    @DisplayName("[관리자] 쿠폰정보 수정 - PERCENTAGE의 discountValue가 0 ~ 100 범위를 넘어감")
    void modifyCouponInfoWithPercentageDiscountValueOutOfRange() throws Exception {
        // Given
        Long couponIdToBeUpdated = 4L;
        CouponModifyRequest request = CouponModifyRequest.builder()
                .name("Winter Sale")
                .discountType(DiscountType.PERCENTAGE.name())
                .discountValue(150L) // Percentage over 100
                .startAt(LocalDateTime.now().plusDays(1))
                .endAt(LocalDateTime.now().plusDays(30))
                .issuedQuantity(50)
                .requiresConcurrencyControl(false)
                .appliesToType(CouponTargetType.PRODUCT.toString())
                .appliesToId(1L)
                .targetImgUrl("https://image.url/winter.jpg")
                .build();

        String requestJson = objectMapper.writeValueAsString(request);

        // When // Then
        mockMvc.perform(
                        patch("/admin/coupons/{id}", couponIdToBeUpdated)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestJson)
                )
                .andDo(print())
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.TEXT_PLAIN_VALUE + ";charset=UTF-8"))
                .andExpect(content().string("Percentage discount value must be between 0 and 100."));
    }

    @DisplayName("[관리자] 쿠폰 삭제 - 유효한 요청/ 존재하는 쿠폰")
    @Test
    void deleteCouponInfoWithExistingCoupon() throws Exception {
        // Given
        Long couponInfoId = 1L; // 존재한다고 가정

        // When // Then
        mockMvc
                .perform(delete("/admin/coupons/{couponInfoId}", couponInfoId))
                .andDo(print())
                .andExpect(status().isNoContent()); // Expect 204 -> successful deletion

        verify(couponService, times(1)).deleteCouponInfoWithAppliesTo(couponInfoId);
    }

    @DisplayName("[관리자] 쿠폰 삭제 - 존재하지 않는 쿠폰")
    @Test
    void deleteCouponInfoWithNonExistingCoupon() throws Exception {
        // Given
        Long couponInfoId = 999L; // 존재하지 않는다고 가정
        doThrow(new EntityNotFoundException("Coupon not found")).when(couponService).deleteCouponInfoWithAppliesTo(anyLong());

        // When // Then
        mockMvc
                .perform(delete("/admin/coupons/{couponInfoId}", couponInfoId))
                .andDo(print())
                .andExpect(status().isNotFound()); // Expect 404 Not Found for a missing coupon

        verify(couponService, times(1)).deleteCouponInfoWithAppliesTo(couponInfoId);
    }

    @DisplayName("[관리자] 쿠폰 발급중지 - 유효한 요청/ 존재하는 쿠폰")
    @Test
    void invalidateCouponInfoWithExistingCoupon() throws Exception {
        // Given
        Long couponInfoId = 1L; // 존재한다고 가정

        // Stubbing CouponService to return the expected ID on modification // 넣은값이 그대로 나올 수 있도록.
        // 이 설정을 안해주면 mockbean은 무조건 0을 뱉는다.
        when(couponService.invalidateCoupon(eq(couponInfoId)))
                .thenReturn(couponInfoId);

        // When // Then
        mockMvc
                .perform(patch("/admin/coupons/{couponInfoId}/invalidate", couponInfoId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").value(couponInfoId));

        verify(couponService, times(1)).invalidateCoupon(couponInfoId);
    }

    @DisplayName("[관리자] 쿠폰 발급중지 - 존재하지 않는 쿠폰")
    @Test
    void invalidateCouponInfoWithNonExistingCoupon() throws Exception {
        // Given
        Long couponInfoId = 999L; // 존재하지 않는다고 가정
        doThrow(new EntityNotFoundException("Coupon not found")).when(couponService).invalidateCoupon(anyLong());

        // When // Then
        mockMvc
                .perform(patch("/admin/coupons/{couponInfoId}/invalidate", couponInfoId))
                .andDo(print())
                .andExpect(status().isNotFound()); // Expect 404 Not Found for a missing coupon

        verify(couponService, times(1)).invalidateCoupon(couponInfoId);
    }

}
