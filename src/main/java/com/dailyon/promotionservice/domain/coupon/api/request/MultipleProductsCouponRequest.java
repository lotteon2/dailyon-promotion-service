package com.dailyon.promotionservice.domain.coupon.api.request;

import lombok.Data;

import java.util.List;


@Data
public class MultipleProductsCouponRequest {
    private List<ProductCategoryPair> products;

    @Data
    public static class ProductCategoryPair {
        private Long productId;
        private Long categoryId;

    }
}