package com.dailyon.promotionservice.domain.coupon.repository.custom;

import com.dailyon.promotionservice.domain.coupon.entity.MemberCoupon;
import com.dailyon.promotionservice.domain.coupon.entity.QCouponAppliesTo;
import com.dailyon.promotionservice.domain.coupon.entity.QCouponInfo;
import com.dailyon.promotionservice.domain.coupon.entity.QMemberCoupon;
import com.querydsl.jpa.JPQLQuery;
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
}
