package com.dailyon.promotionservice.domain.coupon.entity;

public enum CouponType {
    PRODUCT,
    CATEGORY;

    public static CouponType fromString(String type) {
        for (CouponType couponType : CouponType.values()) {
            if (couponType.name().equalsIgnoreCase(type)) {
                return couponType;
            }
        }
        throw new IllegalArgumentException("No enum constant for string value: " + type);
    }
}
