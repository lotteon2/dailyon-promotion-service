package com.dailyon.promotionservice.domain.coupon.entity;

import lombok.Getter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Entity
public class CouponInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Long discountAmount;

    @Column(nullable = false)
    private Integer discountRate;

    @Column(nullable = false)
    private LocalDateTime startAt;

    @Column(nullable = false)
    private LocalDateTime endAt;

    @Column(nullable = false)
    private Integer issuedQuantity;

    @Column(nullable = false)
    private Integer usedQuantity;

    @Column(nullable = false)
    private Boolean requiresConcurrencyControl;

    @Column(nullable = false)
    private String targetImgUrl;
}
