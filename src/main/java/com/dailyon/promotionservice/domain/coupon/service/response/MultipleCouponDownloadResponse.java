package com.dailyon.promotionservice.domain.coupon.service.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;


@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class MultipleCouponDownloadResponse {
    @Builder.Default private List<Long> successfulIds = new ArrayList<>();
    @Builder.Default private List<Long> failedIds = new ArrayList<>();

    public void addSuccess(Long couponInfoId) {
        successfulIds.add(couponInfoId);
    }

    public void addFailure(Long couponInfoId) {
        failedIds.add(couponInfoId);
    }
}
