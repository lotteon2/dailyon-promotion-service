package com.dailyon.promotionservice.domain.coupon.repository.custom;

import com.dailyon.promotionservice.domain.coupon.entity.CouponInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CouponInfoRepositoryCustom {
    Page<CouponInfo> findWithDynamicQuery(Pageable pageable);

    List<CouponInfo> findCouponInfosByProductIdAndCategoryId(long productId, long categoryId);
}
