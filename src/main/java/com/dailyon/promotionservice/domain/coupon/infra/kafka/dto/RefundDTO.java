package com.dailyon.promotionservice.domain.coupon.infra.kafka.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundDTO {
    private ProductInfo productInfo;
    private Long couponInfoId;
    private PaymentInfo paymentInfo;
    private String orderNo;
    private Long memberId;
    private int refundPoints;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductInfo {
        private Long productId;
        private Long sizeId;
        private Long quantity;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentInfo {
        private String orderNo;
        private int cancelAmount;
    }
}
