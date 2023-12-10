package com.dailyon.promotionservice.domain.coupon.entity;

public enum CouponTargetType {
    PRODUCT,
    CATEGORY;

    public static CouponTargetType fromString(String type) {
        for (CouponTargetType couponType : CouponTargetType.values()) {
            if (couponType.name().equalsIgnoreCase(type)) {
                return couponType;
            }
        }
        throw new IllegalArgumentException("No enum constant for string value: " + type);
    }
}
