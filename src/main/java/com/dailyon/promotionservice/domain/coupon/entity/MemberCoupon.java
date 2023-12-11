package com.dailyon.promotionservice.domain.coupon.entity;

import com.dailyon.promotionservice.domain.common.BaseTimeEntity;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;


@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Entity
@IdClass(MemberCoupon.MemberCouponId.class) // composite primary 표현 위한 IdClass
public class MemberCoupon extends BaseTimeEntity {

    @Id
    private Long memberId;

    @Id
    private Long couponInfoId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "couponInfoId", referencedColumnName = "id", insertable = false, updatable = false)
    private CouponInfo couponInfo;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private Boolean isUsed;

    public void markAsUsed() { this.isUsed = true; }

    @AllArgsConstructor
    @NoArgsConstructor
    @EqualsAndHashCode
    @Builder
    public static class MemberCouponId implements Serializable {
        private Long memberId;
        private Long couponInfoId;
    }
}