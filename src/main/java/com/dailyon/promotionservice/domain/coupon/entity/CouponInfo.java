package com.dailyon.promotionservice.domain.coupon.entity;

import com.dailyon.promotionservice.domain.coupon.api.request.CouponModifyRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class CouponInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private DiscountType discountType;

    @Column(nullable = false)
    private Long discountValue;

    @Column(nullable = false)
    private LocalDateTime startAt;

    @Column(nullable = false)
    private LocalDateTime endAt;

    @Column(nullable = false)
    private Integer issuedQuantity;

    @Column(nullable = false)
    private Integer remainingQuantity;

    @Builder.Default
    @Column(nullable = false) // true시, redis 통해 동시관리 들어감.
    private Boolean requiresConcurrencyControl = false;

//    @Column(nullable = false) // 이벤트페이지 전시용. 없을 수 있음.
    private String targetImgUrl;


    public void updateDetails(CouponModifyRequest request) {
        if (request.getName() != null) { this.name = request.getName(); }

        if (request.getDiscountValue() != null && request.getDiscountType() != null) {
            this.discountValue = request.getDiscountValue();
            this.discountType = DiscountType.fromString(request.getDiscountType());
        }

        if (request.getStartAt() != null) { this.startAt = request.getStartAt(); }
        if (request.getEndAt() != null) { this.endAt = request.getEndAt(); }
        if (request.getIssuedQuantity() != null) { this.issuedQuantity = request.getIssuedQuantity(); }
        if (request.getRequiresConcurrencyControl() != null) {
            this.requiresConcurrencyControl = request.getRequiresConcurrencyControl();
        }
        if (request.getTargetImgUrl() != null) { this.targetImgUrl = request.getTargetImgUrl(); }
    }


}
