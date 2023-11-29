package com.dailyon.promotionservice.domain.coupon.repository;

import com.dailyon.promotionservice.domain.coupon.entity.CouponInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;


@Repository
public interface CouponInfoRepository extends JpaRepository<CouponInfo, Long> {

    @Query("SELECT cat.appliesToId FROM CouponAppliesTo cat " +
            "WHERE cat.appliesToId IN :productIds " +
            "AND cat.appliesToType = 'PRODUCT' " +
            "AND CURRENT_TIMESTAMP BETWEEN cat.couponInfo.startAt AND cat.couponInfo.endAt")
    Set<Long> findProductIdsWithCoupons(@Param("productIds") List<Long> productIds);


    @Query("SELECT ci FROM CouponInfo ci " +
            "WHERE " +
            "((ci.appliesTo.appliesToId = :productId AND ci.appliesTo.appliesToType = 'PRODUCT') OR " +
            "(ci.appliesTo.appliesToId = :categoryId AND ci.appliesTo.appliesToType = 'CATEGORY'))" +
            " AND " +
            "CURRENT_TIMESTAMP BETWEEN ci.startAt AND ci.endAt " +
            "AND ci.remainingQuantity > 0")
    List<CouponInfo> findActiveCouponsForProductAndCategory(@Param("productId") long productId,
                                                            @Param("categoryId") long categoryId);

    @Query("SELECT ci FROM CouponInfo ci " +
            "JOIN ci.appliesTo cat " +
            "WHERE cat.appliesToType = 'CATEGORY' " +
            "AND cat.appliesToId = :categoryId " +
            "AND CURRENT_TIMESTAMP BETWEEN ci.startAt AND ci.endAt " +
            "AND ci.remainingQuantity > 0")
    List<CouponInfo> findActiveCouponsForCategory(@Param("categoryId") Long categoryId);
}
