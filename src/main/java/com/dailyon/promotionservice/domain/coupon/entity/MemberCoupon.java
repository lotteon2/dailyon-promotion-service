package com.dailyon.promotionservice.domain.coupon.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;


@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Entity
@IdClass(MemberCoupon.MemberCouponId.class) // composite primary 표현 위한 IdClass
public class MemberCoupon {

    @Id
    private Long memberId; // member service에 있어서 연관관계 맺을 수 없음.

    @Id
    private Long couponInfoId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_info_id", referencedColumnName = "id", insertable = false, updatable = false)
    private CouponInfo couponInfo;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private Boolean isUsed;

    public void markAsUsed() { this.isUsed = true; }

    @AllArgsConstructor
    @Builder
    public static class MemberCouponId implements Serializable {
        private Long memberId;
        private Long couponInfoId;
    }
}