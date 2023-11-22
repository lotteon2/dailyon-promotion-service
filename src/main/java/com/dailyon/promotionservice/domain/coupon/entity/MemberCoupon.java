package com.dailyon.promotionservice.domain.coupon.entity;

import lombok.Getter;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;


@Getter
@Entity
@IdClass(MemberCoupon.MemberCouponId.class) // composite primary 표현 위한 IdClass
public class MemberCoupon {

    @Id
    private Long member_id; // member service에 있어서 연관관계 맺을 수 없음.

    @Id
    private Long coupon_info_id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_info_id", referencedColumnName = "id", insertable = false, updatable = false)
    private CouponInfo couponInfo;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private Boolean isUsed;

    public static class MemberCouponId implements Serializable {
        private Long member_id;
        private Long coupon_info_id;
    }
}