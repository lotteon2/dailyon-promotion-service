package com.dailyon.promotionservice.domain.coupon.repository.custom;

import com.dailyon.promotionservice.domain.coupon.entity.CouponInfo;
import com.dailyon.promotionservice.domain.coupon.entity.QCouponAppliesTo;
import com.dailyon.promotionservice.domain.coupon.entity.QCouponInfo;
import com.dailyon.promotionservice.domain.coupon.entity.QMemberCoupon;
import com.dailyon.promotionservice.domain.coupon.entity.enums.CouponTargetType;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import java.time.LocalDateTime;
import java.util.List;


public class CouponInfoRepositoryCustomImpl extends QuerydslRepositorySupport implements CouponInfoRepositoryCustom {

    public CouponInfoRepositoryCustomImpl() {
        super(CouponInfo.class);
    }

    @Override
    public Page<CouponInfo> findWithDynamicQuery(Pageable pageable) {
        QCouponInfo couponInfo = QCouponInfo.couponInfo;
        JPQLQuery<CouponInfo> query = from(couponInfo)
                .orderBy(couponInfo.updatedAt.desc());

        JPQLQuery<CouponInfo> pageableQuery = getQuerydsl().applyPagination(pageable, query);
        List<CouponInfo> list = pageableQuery.fetch();
        long total = pageableQuery.fetchCount();

        return new PageImpl<>(list, pageable, total);
    }

    @Override
    public List<CouponInfo> findCouponInfosByProductIdAndCategoryId(long productId, long categoryId) {
        QCouponInfo couponInfo = QCouponInfo.couponInfo;
        QCouponAppliesTo couponAppliesTo = QCouponAppliesTo.couponAppliesTo;

        BooleanExpression productCondition = couponInfo.appliesTo.appliesToType.eq(CouponTargetType.PRODUCT)
                .and(couponInfo.appliesTo.appliesToId.eq(productId));
        BooleanExpression categoryCondition = couponInfo.appliesTo.appliesToType.eq(CouponTargetType.CATEGORY)
                .and(couponInfo.appliesTo.appliesToId.eq(categoryId));

        JPQLQuery<CouponInfo> query = from(couponInfo)
                .where(productCondition.or(categoryCondition)
                        .and(couponInfo.startAt.loe(LocalDateTime.now()))
                        .and(couponInfo.endAt.goe(LocalDateTime.now()))
                        .and(couponInfo.remainingQuantity.gt(0)));

        return query.fetch();
    }
}
