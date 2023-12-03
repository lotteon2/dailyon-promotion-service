package com.dailyon.promotionservice.domain.coupon.api;

import com.dailyon.promotionservice.ControllerTestSupport;
import com.dailyon.promotionservice.domain.coupon.service.response.CouponExistenceResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class CouponFeignControllerTest extends ControllerTestSupport {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;


    @DisplayName("product IDs를 기준으로 현재시각 기준 발급가능 쿠폰 조회 - 유효한 요청")
    @Test
    void checkCouponsExistenceByProductIdsWithValidRequest() throws Exception {
        // Given
        List<Long> productIds = Arrays.asList(1L, 2L, 3L);
        List<CouponExistenceResponse> mockResponse = Arrays.asList(
                new CouponExistenceResponse(1L, true),
                new CouponExistenceResponse(2L, true),
                new CouponExistenceResponse(3L, false)
        );
        String productIdsParam = productIds.stream().map(Object::toString).collect(Collectors.joining(","));

        // When CouponService is called, it should return the prepared mock response
        when(couponService.checkCouponsExistenceByProductIds(productIds)).thenReturn(mockResponse);

        // When // Then
        mockMvc.perform(get("/clients/coupons/coupons-existence")
                        .param("productIds", productIdsParam))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(mockResponse.size()))) // Check the size of the returned list
                .andExpect(jsonPath("$[0].productId").value(mockResponse.get(0).getProductId()))
                .andExpect(jsonPath("$[0].hasAvailableCoupon").value(mockResponse.get(0).getHasAvailableCoupon()));
        // Add other jsonPath assertions as needed to validate each object in the array

        verify(couponService, times(1)).checkCouponsExistenceByProductIds(productIds);
    }

    @DisplayName("product IDs를 기준으로 현재시각 기준 발급가능 쿠폰 조회 - 유효하지 않은 요청 (빈 목록)")
    @Test
    void checkCouponsExistenceByEmptyProductIds() throws Exception {
        // When // Then
        mockMvc.perform(get("/clients/coupons/coupons-existence"))
                .andDo(print())
                .andExpect(status().isBadRequest()); // Expect 400 Bad Request for empty productIds

        verify(couponService, never()).checkCouponsExistenceByProductIds(any());
    }
}
