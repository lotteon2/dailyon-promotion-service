package com.dailyon.promotionservice.domain.coupon.infra.kafka.dto;

import com.dailyon.promotionservice.domain.coupon.infra.kafka.dto.enums.OrderEvent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDTO {

    private List<ProductInfo> productInfos;
    private List<Long> couponInfos;
    private PaymentInfo paymentInfo;
    private String orderNo;
    private Long memberId;
    private int usedPoints;
    private OrderEvent orderEvent;
//    TODO: tOrder는 order Service의 dynamoDB 테이블임. nexus repository에 올라오는걸 기다리고 이후 대체할 예정. 현재 로직에 필요 없어서 주석처리함.
//    private TOrder tOrder;


//    private static PaymentInfo createPaymentInfo(String pgToken) {
//        return PaymentInfo.builder().pgToken(pgToken).build();
//    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProductInfo {
        private Long productId;
        private Long sizeId;
        private Long quantity;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PaymentInfo {
        private String pgToken;
    }
}
