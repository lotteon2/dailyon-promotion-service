package com.dailyon.promotionservice.domain.coupon.api.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MultipleProductsCouponRequest {
    private List<ProductCategoryPair> products;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductCategoryPair {
        private Long productId;
        private Long categoryId;

    }
}