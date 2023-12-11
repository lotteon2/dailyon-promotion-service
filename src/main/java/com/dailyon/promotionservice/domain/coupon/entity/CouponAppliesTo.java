package com.dailyon.promotionservice.domain.coupon.entity;

import com.dailyon.promotionservice.domain.coupon.api.request.CouponModifyRequest;
import com.dailyon.promotionservice.domain.coupon.entity.enums.CouponTargetType;
import com.dailyon.promotionservice.domain.coupon.entity.enums.DiscountType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import javax.persistence.*;
import java.io.Serializable;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PACKAGE) // protected 나 package-private 생성자를 사용해 불변성 유지
@Entity
public class CouponAppliesTo implements Serializable {
    // CouponInfo의 ID를 pk로 사용.
    @Id
    private Long couponInfoId;

    @Column(nullable = false)
    private Long appliesToId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CouponTargetType appliesToType;

    @Column(nullable = false)
    private String appliesToName;

    // 식별관계. CouponInfo Entity와의 OneToOne 매핑을 명시.
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL) // "appliesTo"라는 필드 이름으로 명명
    @MapsId // , referencedColumnName = "id"
    @JoinColumn(name = "coupon_info_id")
    private CouponInfo couponInfo;

    @Builder
    protected CouponAppliesTo(CouponInfo couponInfo,
                              Long appliesToId,
                              CouponTargetType appliesToType,
                              String appliesToName) {
        this.couponInfo = couponInfo;
        this.couponInfoId = (couponInfo != null ? couponInfo.getId() : null);
        this.appliesToId = appliesToId;
        this.appliesToType = appliesToType;
        this.appliesToName = appliesToName;
    }

    public void updateDetails(CouponModifyRequest request) {
        if (request.getAppliesToType() != null && request.getAppliesToId() != null) {
            this.appliesToType = CouponTargetType.fromString(request.getAppliesToType());
            this.appliesToId = request.getAppliesToId();
        }

        if (request.getAppliesToName() != null) {
            this.appliesToName = request.getAppliesToName();
        }
    }

    public static CouponAppliesTo createWithCouponInfo(CouponInfo couponInfo,
                                                       Long appliesToId,
                                                       CouponTargetType appliesToType,
                                                       String appliesToName) {

        return CouponAppliesTo.builder()
                .couponInfo(couponInfo)
                .appliesToId(appliesToId)
                .appliesToType(appliesToType)
                .appliesToName(appliesToName)
                .build();
    }

    protected CouponAppliesTo() {} // JPA를 위한 protected 빈 생성자
}
