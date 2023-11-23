package com.dailyon.promotionservice.domain.coupon.api.request;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
public class CouponModifyRequest {

    @NotEmpty
    private String name; // 기본은 FE에서 기본값으로 넣어줌. BE 입장에서는 not null임.

    private Integer discountRate;
    private Long discountAmount;
    // discountRate, discountAmount 두 값 중 하나가 들어오게 됨. isValidDiscount로 구현.

    @NotNull private LocalDateTime startAt;
    @NotNull private LocalDateTime endAt;

    @NotNull private Integer issuedQuantity;

    @NotEmpty private String appliesToType; //coupon 도메인의 entity 패키지에 있는 CouponType ENUM 클래스와 호환
    @NotNull private Long appliesToId;

    @NotNull private Boolean requiresConcurrencyControl;
    private String targetImgUrl;

    public boolean isValidDiscount() {
        return (discountRate == null) != (discountAmount == null); // XOR condition. 둘 중 하나만 있어야 true
    }
}
