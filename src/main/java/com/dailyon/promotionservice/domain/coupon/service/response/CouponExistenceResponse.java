package com.dailyon.promotionservice.domain.coupon.service.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@AllArgsConstructor
@Builder
@Data
public class CouponExistenceResponse {
    private Long productId;
    private Boolean hasCoupons;
}
