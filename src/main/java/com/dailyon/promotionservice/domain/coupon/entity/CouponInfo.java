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

//    @Column(nullable = false) // 두 값중 하나만 값이 있음. 유효성 검사 따로 진행.
    private Long discountAmount;

//    @Column(nullable = false)
    private Integer discountRate;

    @Column(nullable = false)
    private LocalDateTime startAt;

    @Column(nullable = false)
    private LocalDateTime endAt;

    @Column(nullable = false)
    private Integer issuedQuantity;

    @Builder.Default
    @Column(nullable = false)
    private Integer usedQuantity = 0;

    @Builder.Default
    @Column(nullable = false) // true시, redis 통해 동시관리 들어감.
    private Boolean requiresConcurrencyControl = false;

//    @Column(nullable = false) // 이벤트페이지 전시용. 없을 수 있음.
    private String targetImgUrl;


    public void updateDetails(CouponModifyRequest request) {
        if (request.getName() != null) { this.name = request.getName(); }
        if (request.getDiscountAmount() != null) { this.discountAmount = request.getDiscountAmount(); }
        if (request.getDiscountRate() != null) { this.discountRate = request.getDiscountRate(); }
        if (request.getStartAt() != null) { this.startAt = request.getStartAt(); }
        if (request.getEndAt() != null) { this.endAt = request.getEndAt(); }
        if (request.getIssuedQuantity() != null) { this.issuedQuantity = request.getIssuedQuantity(); }
        if (request.getRequiresConcurrencyControl() != null) {
            this.requiresConcurrencyControl = request.getRequiresConcurrencyControl();
        }
        if (request.getTargetImgUrl() != null) { this.targetImgUrl = request.getTargetImgUrl(); }
    }


}
