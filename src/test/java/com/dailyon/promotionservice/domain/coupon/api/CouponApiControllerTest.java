package com.dailyon.promotionservice.domain.coupon.api;

import com.dailyon.promotionservice.ControllerTestSupport;
import com.dailyon.promotionservice.domain.coupon.api.request.CouponCreateRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CouponApiControllerTest extends ControllerTestSupport {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @DisplayName("관리자는 유효한 요청으로 쿠폰을 생성할 수 있다.")
    @Test
    void createCouponInfoWithValidRequest() throws Exception {
        //given
        CouponCreateRequest request = new CouponCreateRequest(
                "New Year Sale",
                10,
                null,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(10),
                100,
                "PRODUCT",
                1L,
                true,
                "https://image.url/target.jpg"
        );

        // when // then
        mockMvc
                .perform(
                        post("/coupons")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").isNotEmpty());
    }


    @Test
    @DisplayName("관리자는 할인율과 할인액을 동시에 설정할 수 없다.")
    void createCouponInfoWithInvalidDiscount() throws Exception {
        // Given
        CouponCreateRequest request = new CouponCreateRequest(
                "New Year Sale",
                10, // discountRate, discountAmount 중 하나만 들어오도록 XOR로 유효성 검사를 해둠.
                5000L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(10),
                100,
                "PRODUCT",
                1L,
                true,
                "https://image.url/target.jpg"
        );

        // When // Then
        mockMvc
                .perform(
                        post("/coupons")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Invalid discount: either rate or amount must be set, not both"));
    }


    @Test
    @DisplayName("관리자는 시작일 없이 쿠폰을 생성하려고 할 때 실패해야 한다.")
    void createCouponInfoWithNullStartAt() throws Exception {
        // Given
        CouponCreateRequest request = new CouponCreateRequest(
                "New Year Sale",
                10,
                null,
                null, // startAt is null - 필수데이터 하나만 예시로 제거했음.
                LocalDateTime.now().plusDays(10),
                100,
                "PRODUCT",
                1L,
                true,
                "https://image.url/target.jpg"
        );

        // When // Then
        mockMvc
                .perform(
                        post("/coupons")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Invalid request"))
                .andExpect(jsonPath("$.validation.startAt").value("startAt can not be null"));
    }


}
