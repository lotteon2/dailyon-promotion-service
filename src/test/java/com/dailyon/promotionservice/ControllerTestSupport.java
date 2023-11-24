package com.dailyon.promotionservice;

import com.dailyon.promotionservice.domain.coupon.api.CouponApiController;
import com.dailyon.promotionservice.domain.coupon.service.CouponService;
import com.dailyon.promotionservice.domain.event.api.EventApiController;
import com.dailyon.promotionservice.domain.event.service.EventService;
import com.dailyon.promotionservice.domain.raffle.api.RaffleApiController;
import com.dailyon.promotionservice.domain.raffle.service.RaffleService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
        controllers = {
                CouponApiController.class,
                RaffleApiController.class,
                EventApiController.class
        })
public class ControllerTestSupport {

    @Autowired protected MockMvc mockMvc;

    @Autowired protected ObjectMapper objectMapper;

    @MockBean protected CouponService couponService;

    @MockBean protected RaffleService raffleService;

    @MockBean protected EventService eventService;

//    @MockBean protected StorageApiClient storageApiClient;
}
