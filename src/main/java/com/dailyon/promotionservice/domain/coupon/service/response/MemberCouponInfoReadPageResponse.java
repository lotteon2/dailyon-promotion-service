package com.dailyon.promotionservice.domain.coupon.service.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class MemberCouponInfoReadPageResponse {
    private List<MemberCouponInfoReadItemResponse> memberCouponInfoReadItemResponse;
    private Long totalCounts;
}
