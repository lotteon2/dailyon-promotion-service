package com.dailyon.promotionservice.domain.coupon.api.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CouponValidationRequest {
    @NotNull private Long productId;
    @NotNull private Long categoryId;
    @NotNull private Long couponInfoId;
}
