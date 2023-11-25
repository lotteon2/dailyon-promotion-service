package com.dailyon.promotionservice.domain.coupon.entity;

public enum DiscountType {
    FIXED_AMOUNT,
    PERCENTAGE;

    public static DiscountType fromString(String type) {
        for (DiscountType discountType : DiscountType.values()) {
            if (discountType.name().equalsIgnoreCase(type)) {
                return discountType;
            }
        }
        throw new IllegalArgumentException("No enum constant for string value: " + type);
    }
}
