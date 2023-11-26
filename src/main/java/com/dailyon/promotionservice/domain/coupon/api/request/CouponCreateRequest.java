package com.dailyon.promotionservice.domain.coupon.api.request;

import com.dailyon.promotionservice.domain.coupon.entity.CouponInfo;
import com.dailyon.promotionservice.domain.coupon.entity.DiscountType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

import static com.dailyon.promotionservice.domain.coupon.entity.DiscountType.fromString;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CouponCreateRequest {

    @NotEmpty private String name; // 기본은 FE에서 기본값으로 넣어줌. BE 입장에서는 not null임.

    @NotEmpty private String discountType; //coupon 도메인의 entity 패키지에 있는 DiscountType ENUM 클래스와 호환
    @NotNull private Long discountValue;

    @NotNull private LocalDateTime startAt;
    @NotNull private LocalDateTime endAt;

    @NotNull private Integer issuedQuantity;

    @NotEmpty private String appliesToType; //coupon 도메인의 entity 패키지에 있는 CouponType ENUM 클래스와 호환
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

    @JsonIgnore // 테스트케이스 돌릴 때, Jackson에서 멋대로 작동시켜서 에러를 낸 것에 대한 처리. 다른 방법 고민 중.
    public boolean isValidDateRange() {
        return startAt.isBefore(endAt);
    }

    public CouponInfo toEntity() { // CouponCreateRequest 요청의 appliesToType, appliesToId는 CouponAppliesTo Entity에 매핑될것임.
        return CouponInfo.builder()
                .name(name)
                .discountType( fromString(discountType) )
                .discountValue(discountValue)
                .startAt(startAt)
                .endAt(endAt)
                .issuedQuantity(issuedQuantity)
                .remainingQuantity(issuedQuantity)
                .requiresConcurrencyControl(requiresConcurrencyControl)
                .targetImgUrl(targetImgUrl)
                .build();

    }
}