package com.dailyon.promotionservice.domain.coupon.repository;

import com.dailyon.promotionservice.domain.coupon.entity.CouponInfo;
import com.dailyon.promotionservice.domain.coupon.repository.custom.CouponInfoRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;


@Repository
public interface CouponInfoRepository extends JpaRepository<CouponInfo, Long>, QuerydslPredicateExecutor<CouponInfo>, CouponInfoRepositoryCustom {


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

    // TODO: 쿼리를 2개로 나눠서 appliesId: List<CouponInfo> Map을 반환하는 aggregation으로 변경하고,
    //  map을 통해 list를 넣어주는 로직으로 고도화.
    @Query("SELECT ci FROM CouponInfo ci " +
            "WHERE " +
            "((ci.appliesTo.appliesToId IN :productIds AND ci.appliesTo.appliesToType = 'PRODUCT') OR " +
            "(ci.appliesTo.appliesToId IN :categoryIds AND ci.appliesTo.appliesToType = 'CATEGORY'))" +
            " AND " +
            "CURRENT_TIMESTAMP BETWEEN ci.startAt AND ci.endAt " +
            "AND ci.remainingQuantity > 0")
    List<CouponInfo> findActiveCouponsForProductsAndCategories(@Param("productIds") List<Long> productIds,
                                                               @Param("categoryIds") List<Long> categoryIds);


    @Query("SELECT ci FROM CouponInfo ci " +
            "JOIN ci.appliesTo cat " +
            "WHERE cat.appliesToType = 'CATEGORY' " +
            "AND cat.appliesToId = :categoryId " +
            "AND CURRENT_TIMESTAMP BETWEEN ci.startAt AND ci.endAt " +
            "AND ci.remainingQuantity > 0")
    List<CouponInfo> findActiveCouponsForCategory(@Param("categoryId") Long categoryId);
}
