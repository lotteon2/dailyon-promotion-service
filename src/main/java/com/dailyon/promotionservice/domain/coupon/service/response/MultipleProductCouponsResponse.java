package com.dailyon.promotionservice.domain.coupon.service.response;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class MultipleProductCouponsResponse {
    private Map<Long, List<CouponInfoItemResponse>> coupons;

    public MultipleProductCouponsResponse(Map<Long, List<CouponInfoItemResponse>> coupons) {
        this.coupons = coupons;
    }
}
