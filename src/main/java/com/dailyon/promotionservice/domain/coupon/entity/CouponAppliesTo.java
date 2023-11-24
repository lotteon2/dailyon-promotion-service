package com.dailyon.promotionservice.domain.coupon.entity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import javax.persistence.*;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PACKAGE) // protected 나 package-private 생성자를 사용해 불변성 유지
@Entity
public class CouponAppliesTo {
    // CouponInfo의 ID를 pk로 사용.
    @Id
    private Long couponInfoId;

    @Column(nullable = false)
    private Long appliesToId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CouponType appliesToType;

    // 식별관계. CouponInfo Entity와의 OneToOne 매핑을 명시.
    @OneToOne
    @MapsId
    @JoinColumn(name = "coupon_info_id")
    private CouponInfo couponInfo;

    @Builder
    protected CouponAppliesTo(CouponInfo couponInfo, Long appliesToId, CouponType appliesToType) {
        this.couponInfo = couponInfo;
        this.couponInfoId = (couponInfo != null ? couponInfo.getId() : null);
        this.appliesToId = appliesToId;
        this.appliesToType = appliesToType;
    }

    public static CouponAppliesTo createWithCouponInfo(CouponInfo couponInfo, Long appliesToId, CouponType appliesToType) {
        if (couponInfo == null || couponInfo.getId() == null) {
            throw new IllegalStateException("CouponInfo must be persisted before creating CouponAppliesTo");
        }

        return CouponAppliesTo.builder()
                .couponInfo(couponInfo)
                .appliesToId(appliesToId)
                .appliesToType(appliesToType)
                .build();
    }

    protected CouponAppliesTo() {} // JPA를 위한 protected 빈 생성자
}
