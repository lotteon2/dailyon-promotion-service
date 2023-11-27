package com.dailyon.promotionservice.domain.coupon.entity;

import com.dailyon.promotionservice.domain.coupon.api.request.CouponModifyRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class CouponInfo implements Serializable {
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
    @Column(nullable = false) // true시, redis 통해 동시성 관리 들어감.
    private Boolean requiresConcurrencyControl = false;

    // 이벤트페이지 전시용. 없을 수 있음.
    private String targetImgUrl;

    @OneToOne(mappedBy = "couponInfo",fetch = FetchType.LAZY)
    private CouponAppliesTo appliesTo;


    // TODO: Entity는 순수하게 관리되어야 함. request객체(web layer)를 의존하지 않는 설계 고민 중.
    //  update 로직을 service에 두면 setter를 쓰지 않기 위해 모든 필드에 대한 수정 메소드를 정의해야 하고,
    //  수정 메소드의 엔티티 필드들을 각각 param으로 받아서 바꾸려고 하면 param이 너무 많아져버린다.
    //  둘 다 최고의 방법은 아닌듯 함.
    public void updateDetails(CouponModifyRequest request) {
        if (request.getName() != null) { this.name = request.getName(); }

        if (request.getDiscountValue() != null && request.getDiscountType() != null) {
            this.discountValue = request.getDiscountValue();
            this.discountType = DiscountType.fromString(request.getDiscountType());
        }

        if (request.getStartAt() != null) { this.startAt = request.getStartAt(); }
        if (request.getEndAt() != null) { this.endAt = request.getEndAt(); }
        if (request.getIssuedQuantity() != null) {
            this.issuedQuantity = request.getIssuedQuantity();
            this.remainingQuantity = request.getIssuedQuantity();

        }
        if (request.getRequiresConcurrencyControl() != null) {
            this.requiresConcurrencyControl = request.getRequiresConcurrencyControl();
        }
        if (request.getTargetImgUrl() != null) { this.targetImgUrl = request.getTargetImgUrl(); }
    }


    public void remove() {
        this.appliesTo = null;
    }
}
