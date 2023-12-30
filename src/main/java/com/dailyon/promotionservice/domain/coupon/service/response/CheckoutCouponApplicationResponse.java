package com.dailyon.promotionservice.domain.coupon.service.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class CheckoutCouponApplicationResponse {
    private List<List<CouponInfoItemCheckoutResponse>> nestedCouponInfoItemResponses;
}
