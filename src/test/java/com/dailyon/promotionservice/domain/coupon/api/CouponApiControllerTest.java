package com.dailyon.promotionservice.domain.coupon.api;

import com.dailyon.promotionservice.ControllerTestSupport;
import com.dailyon.promotionservice.domain.coupon.api.request.CouponCreateRequest;
import com.dailyon.promotionservice.domain.coupon.api.request.CouponModifyRequest;
import com.dailyon.promotionservice.domain.coupon.entity.CouponTargetType;
import com.dailyon.promotionservice.domain.coupon.entity.DiscountType;
import com.dailyon.promotionservice.domain.coupon.exceptions.InvalidDiscountException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertTrue;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;

//@SpringBootTest
public class CouponApiControllerTest extends ControllerTestSupport {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @DisplayName("관리자는 유효한 요청으로 쿠폰을 생성할 수 있다.")
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
                        post("/coupons")
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
    @DisplayName("관리자는 시작일 없이 쿠폰을 생성하려고 할 때 실패해야 한다.")
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
                        post("/coupons")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0]").value("startAt: must not be null"));
    }


    @Test
    @DisplayName("startAt이 endAt보다 늦을 때 쿠폰 생성 시 오류가 발생한다.")
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
                        post("/coupons")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.TEXT_PLAIN_VALUE + ";charset=UTF-8"))
                .andExpect(content().string("The start date must be before the end date"));
    }


    @DisplayName("Fixed amount discount value must be non-negative")
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
                        post("/coupons")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.TEXT_PLAIN_VALUE + ";charset=UTF-8"))
                .andExpect(content().string("Fixed amount discount value must be non-negative."));
    }


    @DisplayName("Percentage discount value must be between 0 and 100")
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
                        post("/coupons")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.TEXT_PLAIN_VALUE + ";charset=UTF-8"))
                .andExpect(content().string("Percentage discount value must be between 0 and 100."));
    }




//    @DisplayName("관리자는 유효한 요청으로 쿠폰 정보를 수정할 수 있다.")
//    @Test
//    void modifyCouponInfoWithValidRequest() throws Exception {
//        // Given
//        Long couponIdToBeUpdated = 3L;
//        CouponModifyRequest request = new CouponModifyRequest(
//                "Updated Name",
//                15, // Updated discount rate
//                null, // No discount amount since we have a rate
//                LocalDateTime.now().minusDays(10),
//                LocalDateTime.now().plusDays(20),
//                500, // Updated issued quantity
//                "PRODUCT",
//                2L, // Updated applies to ID (maybe a different product/category)
//                false,
//                "https://image.url/updated.jpg"
//        );
//
//        // Make sure the request object is valid
//        assertTrue(request.isValidDiscount(), "Discount information is not valid");
//
//        String requestJson = objectMapper.writeValueAsString(request);
//
//        // Stubbing CouponService to return the expected ID on modification // 넣은값이 그대로 나올 수 있도록.
//        // 이 설정을 안해주면 mockbean은 무조건 0을 뱉는다.
//        when(couponService.modifyCouponInfo(any(CouponModifyRequest.class), eq(couponIdToBeUpdated)))
//                .thenReturn(couponIdToBeUpdated);
//
//        // When // Then
//        mockMvc.perform(
//                        patch("/coupons/{id}", couponIdToBeUpdated)
//                                .contentType(MediaType.APPLICATION_JSON)
//                                .content(requestJson)
//                )
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                .andExpect(jsonPath("$").value(couponIdToBeUpdated));
//    }
//
//    @DisplayName("쿠폰정보수정 - discountRate, discountAmount 동시에 주면 에러.")
//    @Test
//    void modifyCouponInfoWithInvalidRequest() throws Exception {
//        // Given
//        Long couponIdToBeUpdated = 3L;
//        CouponModifyRequest invalidRequest = CouponModifyRequest.builder()
//                .name("Updated Name")
//                .discountRate(15)       // Both discount rate
//                .discountAmount(1000L)  // and discount amount are provided which is invalid
//                .startAt(LocalDateTime.now().minusDays(10))
//                .endAt(LocalDateTime.now().plusDays(20))
//                .issuedQuantity(500)
//                .appliesToType("PRODUCT")
//                .appliesToId(2L)
//                .requiresConcurrencyControl(false)
//                .targetImgUrl("https://image.url/updated.jpg")
//                .build();
//
//        String requestJson = objectMapper.writeValueAsString(invalidRequest);
//
//        // Stubbing CouponService to throw an InvalidDiscountException when invalid request is processed
//        doThrow(new InvalidDiscountException())
//                .when(couponService)
//                .modifyCouponInfo(any(CouponModifyRequest.class), eq(couponIdToBeUpdated));
//
//        // When // Then
//        mockMvc.perform(
//                        patch("/coupons/{id}", couponIdToBeUpdated)
//                                .contentType(MediaType.APPLICATION_JSON)
//                                .content(requestJson)
//                )
//                .andExpect(status().isBadRequest());
//    }
//
//    @Test
//    @DisplayName("쿠폰 수정 - startAt이 endAt보다 늦을 시 오류.")
//    void modifyCouponInfoWithInvalidDateRange() throws Exception {
//        // Given
//        Long couponInfoId = 1L;
//        LocalDateTime startDateTime = LocalDateTime.now().plusDays(10);
//        LocalDateTime endDateTime = LocalDateTime.now().plusDays(1);
//        CouponModifyRequest request = new CouponModifyRequest(
//                "Spring Sale Updated",
//                null,
//                10000L,
//                startDateTime,
//                endDateTime,
//                200,
//                "CATEGORY",
//                2L,
//                true,
//                "https://image.url/updated_target.jpg"
//        );
//
//        // When // Then
//        mockMvc
//                .perform(
//                        patch("/coupons/" + couponInfoId)
//                                .contentType(MediaType.APPLICATION_JSON)
//                                .content(objectMapper.writeValueAsString(request))
//                )
//                .andDo(print())
//                .andExpect(status().isBadRequest())
//                .andExpect(content().contentType(MediaType.TEXT_PLAIN_VALUE + ";charset=UTF-8"))
//                .andExpect(content().string("The start date must be before the end date"));
//    }


}
