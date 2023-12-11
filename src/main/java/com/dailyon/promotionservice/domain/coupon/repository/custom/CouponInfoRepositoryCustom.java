package com.dailyon.promotionservice.domain.coupon.repository.custom;

import com.dailyon.promotionservice.domain.coupon.entity.CouponInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CouponInfoRepositoryCustom {
    Page<CouponInfo> findWithDynamicQuery(Pageable pageable);
}
