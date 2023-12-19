package com.dailyon.promotionservice.domain.coupon.api.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;


@Data
@Builder
@AllArgsConstructor
public class MultipleProductsCouponRequest {
    private List<ProductCategoryPair> products;

    @Data
    @Builder
    @AllArgsConstructor
    public static class ProductCategoryPair {
        private Long productId;
        private Long categoryId;

    }
}