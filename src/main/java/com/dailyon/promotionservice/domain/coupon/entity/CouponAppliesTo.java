package com.dailyon.promotionservice.domain.coupon.entity;

import lombok.Getter;

import javax.persistence.*;

@Getter
@Entity
public class CouponAppliesTo {
    // CouponInfo의 ID를 pk로 사용.
    @Id
    private Long couponInfoId;

    private Long appliesToId;

    @Enumerated(EnumType.STRING)
    private CouponType couponType;

    // 식별관계. CouponInfo Entity와의 OneToOne 매핑을 명시.
    @OneToOne
    @MapsId
    @JoinColumn(name = "coupon_info_id")
    private CouponInfo couponInfo;
}
