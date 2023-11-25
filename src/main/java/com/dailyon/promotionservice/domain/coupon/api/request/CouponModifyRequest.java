package com.dailyon.promotionservice.domain.coupon.api.request;

import com.dailyon.promotionservice.domain.coupon.entity.DiscountType;
import lombok.*;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CouponModifyRequest {

    @NotEmpty private String name; // 기본은 FE에서 기본값으로 넣어줌. BE 입장에서는 not null임.

    @NotEmpty private String discountType; //coupon 도메인의 entity 패키지에 있는 DiscountType ENUM 클래스와 호환
    @NotNull private Long discountValue;

    @NotNull private LocalDateTime startAt;
    @NotNull private LocalDateTime endAt;

    @NotNull private Integer issuedQuantity;

    @NotEmpty private String appliesToType; //coupon 도메인의 entity 패키지에 있는 CouponTargetType ENUM 클래스와 호환
    @NotNull private Long appliesToId;

    @NotNull private Boolean requiresConcurrencyControl;
    private String targetImgUrl;

    // 할인 유효성에 대한 검증 결과를 String으로 반환. 유효할 경우 null을 반환.
    public String getInvalidDiscountMessage() {
        DiscountType type = DiscountType.fromString(discountType);

        switch (type) {
            case FIXED_AMOUNT:
                if (discountValue < 0) {
                    return "Fixed amount discount value must be non-negative.";
                }
                break;
            case PERCENTAGE:
                if (discountValue < 0 || discountValue > 100) {
                    return "Percentage discount value must be between 0 and 100.";
                }
                break;
            default:
                return "Unknown discount type: " + discountType;
        }
        return null; // 유효성 검사를 통과
    }

    public boolean isValidDateRange() {
        return startAt.isBefore(endAt);
    }
}
