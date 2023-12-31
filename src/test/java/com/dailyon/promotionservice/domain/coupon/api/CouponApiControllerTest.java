package com.dailyon.promotionservice.domain.coupon.api;

import com.dailyon.promotionservice.ControllerTestSupport;
import com.dailyon.promotionservice.domain.coupon.api.request.MultipleProductsCouponRequest;
import com.dailyon.promotionservice.domain.coupon.entity.enums.CouponTargetType;
import com.dailyon.promotionservice.domain.coupon.entity.enums.DiscountType;
import com.dailyon.promotionservice.domain.coupon.service.response.CheckoutCouponApplicationResponse;
import com.dailyon.promotionservice.domain.coupon.service.response.CouponInfoItemCheckoutResponse;
import com.dailyon.promotionservice.domain.coupon.service.response.CouponInfoItemResponse;
import com.dailyon.promotionservice.domain.coupon.service.response.CouponInfoItemWithAvailabilityResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNull;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class CouponApiControllerTest extends ControllerTestSupport {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @Test
    @DisplayName("단일 상품에 적용되는 쿠폰 조회 productId and categoryId - 유효한 요청")
    void whenGetSingleProductCouponWithValidRequest_thenReturnsListOfCoupons() throws Exception {
        // Given
        long productId = 1L;
        long categoryId = 100L;
        List<CouponInfoItemResponse> mockResponse = Arrays.asList(
                new CouponInfoItemResponse(1L, CouponTargetType.PRODUCT, 1L, DiscountType.FIXED_AMOUNT, 5000L, LocalDateTime.now().plusDays(5), 0L, null),
                new CouponInfoItemResponse(1L, CouponTargetType.CATEGORY, 1L, DiscountType.PERCENTAGE, 10L, LocalDateTime.now().plusWeeks(1), 0L, null)
        );
        when(couponService.getActiveCouponsForProductAndCategory(productId, categoryId)).thenReturn(mockResponse);

        // When // Then
        mockMvc.perform(get("/coupons/single-product")
                        .param("productId", String.valueOf(productId))
                        .param("categoryId", String.valueOf(categoryId)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(mockResponse.size())));

        // service method 1번만 call 되는걸 검증
        verify(couponService, times(1)).getActiveCouponsForProductAndCategory(productId, categoryId);
    }

    @Test
    @DisplayName("단일 상품에 적용되는 쿠폰 조회 productId and categoryId - Missing Parameters")
    void whenGetSingleProductCouponWithoutParameters_thenBadRequest() throws Exception {
        // When // Then
        mockMvc.perform(get("/coupons/single-product"))
                .andExpect(status().isBadRequest());

        // bad request로 service method 호출 안됨을 검증.
        verify(couponService, never()).getActiveCouponsForProductAndCategory(anyLong(), anyLong());
    }

    @Test
    @DisplayName("조회: 카테고리에 적용되는 쿠폰 - 유효한 요청")
    void whenGetCategoryCouponWithValidRequest_thenReturnsListOfCoupons() throws Exception {
        // Given
        long categoryId = 1L;
        List<CouponInfoItemResponse> mockResponse = Arrays.asList(
                new CouponInfoItemResponse(1L, CouponTargetType.CATEGORY, 1L, DiscountType.FIXED_AMOUNT, 5000L, LocalDateTime.now().plusDays(5), 0L, null),
                new CouponInfoItemResponse(1L, CouponTargetType.CATEGORY, 1L, DiscountType.PERCENTAGE, 10L, LocalDateTime.now().plusWeeks(1), 0L, null)
        );
        when(couponService.getActiveCouponsForCategory(categoryId)).thenReturn(mockResponse);

        // When // Then
        mockMvc.perform(get("/coupons")
                        .param("categoryId", String.valueOf(categoryId)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(mockResponse.size())));

        // Verify that our service method was called once
        verify(couponService, times(1)).getActiveCouponsForCategory(categoryId);
    }

    @Test
    @DisplayName("조회: 카테고리에 적용되는 쿠폰 - 파라미터 누락")
    void whenGetCategoryCouponWithoutParameters_thenBadRequest() throws Exception {
        // When // Then
        mockMvc.perform(get("/coupons"))
                .andExpect(status().isBadRequest());

        // bad request로 service method 호출 안됨을 검증.
        verify(couponService, never()).getActiveCouponsForCategory(anyLong());
    }

    @Test
    @DisplayName("쿠폰 다운로드- 유효한 요청")
    void whenDownloadCouponWithValidRequest_thenSuccess() throws Exception {
        // Given
        Long memberId = 1L;
        Long couponId = 1L;
        doNothing().when(couponService).downloadCoupon(memberId, couponId);

        // When // Then
        mockMvc.perform(post("/coupons/{coupon_id}/download", couponId)
                        .header("memberId", String.valueOf(memberId)))
                .andExpect(status().isOk())
                .andExpect(content().string("성공적으로 쿠폰을 다운로드 했습니다."));

        // 1회 호출 검증
        verify(couponService).downloadCoupon(memberId, couponId);
    }

    @Test
    @DisplayName("쿠폰 다운로드- memberId 헤더 누락")
    void whenDownloadCouponWithoutMemberId_thenBadRequest() throws Exception {
        // Given
        Long couponId = 1L;

        // When // Then
        mockMvc.perform(post("/coupons/{coupon_id}/download", couponId))
                .andExpect(status().isBadRequest());

        // 호출 안됨을 검증
        verify(couponService, never()).downloadCoupon(anyLong(), anyLong());
    }

    @Test
    @DisplayName("단일상품에 대한 쿠폰 리스트 반환[다운로드 가능 여부 포함] - 유효한 요청")
    void whenGetSingleProductCouponWithAvailability_thenReturnsListOfCouponsWithAvailability() throws Exception {
        // Given
        long memberId = 1L;
        long productId = 1L;
        long categoryId = 100L;
        List<CouponInfoItemWithAvailabilityResponse> mockResponse = Collections.singletonList(
                CouponInfoItemWithAvailabilityResponse.builder()
                        .appliesToType(CouponTargetType.PRODUCT)
                        .appliedToId(productId)
                        .discountType(DiscountType.FIXED_AMOUNT)
                        .discountValue(5000L)
                        .endAt(LocalDateTime.now().plusDays(5))
                        .isDownloadable(true)
                        .build()
        );
        when(couponService.getActiveCouponsForProductAndCategoryWithAvailability(memberId, productId, categoryId)).thenReturn(mockResponse);

        // When // Then
        mockMvc.perform(get("/coupons/single-product/with-availability")
                        .header("memberId", memberId) // Using header for memberId in this case
                        .param("productId", String.valueOf(productId))
                        .param("categoryId", String.valueOf(categoryId)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(mockResponse.size())))
                .andExpect(jsonPath("$[0].isDownloadable", is(true)));

        // 1회 호출 검증
        verify(couponService, times(1)).getActiveCouponsForProductAndCategoryWithAvailability(memberId, productId, categoryId);
    }

    
    @Test
    @DisplayName("주문서 적용가능 쿠폰 리스트 반환 - 유효한 요청")
    void whenGetCouponsForCheckout_thenReturnsApplicableCouponList() throws Exception {
        // Given
        Long memberId = 1L;
        MultipleProductsCouponRequest request = new MultipleProductsCouponRequest(Arrays.asList(MultipleProductsCouponRequest.ProductCategoryPair.builder()
                .productId(1L)
                .categoryId(1L)
                .build()));
        
        List<List<CouponInfoItemCheckoutResponse>> nestedCouponInfoItemResponses = Arrays.asList(
                Collections.singletonList(
                    CouponInfoItemCheckoutResponse.builder()
                        .couponInfoId(1L)
                        .couponInfoName("나이키 에어포스 1 특가 할인")
                        .appliesToType(CouponTargetType.PRODUCT)
                        .appliedToId(1L)
                        .discountType(DiscountType.FIXED_AMOUNT)
                        .discountValue(15000L)
                        .endAt(LocalDateTime.parse("2023-12-31T23:59:59"))
                        .minPurchaseAmount(0L)
                        .maxDiscountAmount(null)
                        .build()
                )
        );

        CheckoutCouponApplicationResponse mockResponse = CheckoutCouponApplicationResponse.builder()
                .nestedCouponInfoItemResponses(nestedCouponInfoItemResponses)
                .build();

        when(couponService.findApplicableCoupons(memberId, request)).thenReturn(mockResponse);

        String jsonRequest = objectMapper.writeValueAsString(request);

        // When // Then
        mockMvc.perform(post("/coupons/retrieve-applicable")
                        .header("memberId", memberId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.nestedCouponInfoItemResponses", hasSize(nestedCouponInfoItemResponses.size())))
                .andExpect(jsonPath("$.nestedCouponInfoItemResponses[0][0].couponInfoName", is("나이키 에어포스 1 특가 할인")));

        // 1회 호출 검증
        verify(couponService, times(1)).findApplicableCoupons(memberId, request);
    }
    
    
}
