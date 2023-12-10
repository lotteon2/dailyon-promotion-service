package com.dailyon.promotionservice.domain.coupon.service.response;

import com.dailyon.promotionservice.domain.coupon.entity.CouponInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
public class CouponInfoReadPageResponse {
    private List<CouponInfoReadItemResponse> CouponInfoReadItemResponseList;
    private Long totalCounts; // CouponInfo 테이블의 전체 tuple 개수

    public static CouponInfoReadPageResponse fromPage(Page<CouponInfo> page) {
        List<CouponInfoReadItemResponse> items = page.getContent().stream()
                .map(CouponInfoReadItemResponse::fromEntity)
                .collect(Collectors.toList());

        // getTotalElements는 테이블의 전체 tuple 개수를 반환. getTotalPages와 대비
        return new CouponInfoReadPageResponse(items, page.getTotalElements());
    }



}
