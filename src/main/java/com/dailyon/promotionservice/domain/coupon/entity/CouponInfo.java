package com.dailyon.promotionservice.domain.coupon.entity;

import lombok.Getter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Getter
@Entity
public class CouponInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private Long discountAmount;
    private Integer discountRate;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private Integer issuedQuantity;
    private Integer usedQuantity;
    private Boolean requiresConcurrencyControl;
    private String targetImgUrl;
}
