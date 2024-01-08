package com.dailyon.promotionservice.domain.coupon.repository.custom;

import com.dailyon.promotionservice.domain.coupon.entity.MemberCoupon;
import com.dailyon.promotionservice.domain.coupon.entity.QCouponAppliesTo;
import com.dailyon.promotionservice.domain.coupon.entity.QCouponInfo;
import com.dailyon.promotionservice.domain.coupon.entity.QMemberCoupon;
import com.querydsl.core.QueryResults;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import java.time.LocalDateTime;
import java.util.List;

public class MemberCouponRepositoryCustomImpl extends QuerydslRepositorySupport implements MemberCouponRepositoryCustom {
    public MemberCouponRepositoryCustomImpl() {
        super(MemberCoupon.class);
    }

    @Override
    public List<MemberCoupon> findActiveAndUnusedCouponsByMemberId(Long memberId) {
        QMemberCoupon qMemberCoupon = QMemberCoupon.memberCoupon;
        QCouponInfo qCouponInfo = QCouponInfo.couponInfo;
        QCouponAppliesTo qCouponAppliesTo = QCouponAppliesTo.couponAppliesTo;

        LocalDateTime now = LocalDateTime.now();

        JPQLQuery<MemberCoupon> query = from(qMemberCoupon)
                .innerJoin(qMemberCoupon.couponInfo, qCouponInfo).fetchJoin()
                .innerJoin(qCouponInfo.appliesTo, qCouponAppliesTo).fetchJoin()
                .where(qMemberCoupon.memberId.eq(memberId)
                        .and(qMemberCoupon.isUsed.eq(false))
                        .and(qCouponInfo.startAt.loe(now))
                        .and(qCouponInfo.endAt.goe(now)));

        return query.fetch();
    }

    @Override
    public List<MemberCoupon> findMemberCouponsByMemberIdAndCouponInfoIds(Long memberId, List<Long> couponInfoIds) {
        QMemberCoupon qMemberCoupon = QMemberCoupon.memberCoupon;
        QCouponInfo qCouponInfo = QCouponInfo.couponInfo;

        LocalDateTime now = LocalDateTime.now();

        JPQLQuery<MemberCoupon> query = from(qMemberCoupon)
                .innerJoin(qMemberCoupon.couponInfo, qCouponInfo).fetchJoin()
                .where(qMemberCoupon.memberId.eq(memberId)
                        .and(qMemberCoupon.couponInfoId.in(couponInfoIds)));

        return query.fetch();
    }

    @Override
    public Page<MemberCoupon> findMemberCouponsWithCouponInfoByMemberId(Long memberId, Pageable pageable) {
        QMemberCoupon memberCoupon = QMemberCoupon.memberCoupon;
        QCouponInfo couponInfo = QCouponInfo.couponInfo;
        LocalDateTime now = LocalDateTime.now();


        JPQLQuery<MemberCoupon> query = from(memberCoupon)
                .innerJoin(memberCoupon.couponInfo, couponInfo).fetchJoin()
                .where(memberCoupon.memberId.eq(memberId)
                        .and(memberCoupon.isUsed.eq(false))
                        .and(couponInfo.startAt.loe(now))
                        .and(couponInfo.endAt.goe(now))
                )
                .orderBy(memberCoupon.createdAt.desc());

        JPQLQuery<MemberCoupon> pageableQuery = getQuerydsl().applyPagination(pageable, query);
        QueryResults<MemberCoupon> queryResults = pageableQuery.fetchResults();

        return new PageImpl<>(queryResults.getResults(), pageable, queryResults.getTotal());
    }
}
