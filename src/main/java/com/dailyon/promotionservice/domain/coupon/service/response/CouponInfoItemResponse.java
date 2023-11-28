package com.dailyon.promotionservice.domain.coupon.service.response;

import com.dailyon.promotionservice.domain.coupon.entity.CouponTargetType;
import com.dailyon.promotionservice.domain.coupon.entity.DiscountType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@AllArgsConstructor
@Builder
@Data
public class CouponInfoItemResponse {
    private CouponTargetType appliesToType;
    private Long appliedToId;

    private DiscountType discountType;
    private Long discountValue;
    private LocalDateTime endAt;
}
