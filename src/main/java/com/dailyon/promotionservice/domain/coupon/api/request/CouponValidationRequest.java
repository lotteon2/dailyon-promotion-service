package com.dailyon.promotionservice.domain.coupon.api.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;

@AllArgsConstructor
@Builder
@Data
public class CouponValidationRequest {
    @NotNull private Long productId;
    @NotNull private Long categoryId;
    @NotNull private Long couponInfoId;
}
