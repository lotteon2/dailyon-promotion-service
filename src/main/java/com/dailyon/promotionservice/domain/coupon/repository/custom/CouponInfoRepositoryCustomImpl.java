package com.dailyon.promotionservice.domain.coupon.repository.custom;

import com.dailyon.promotionservice.domain.coupon.entity.CouponInfo;
import com.dailyon.promotionservice.domain.coupon.entity.QCouponInfo;
import com.querydsl.jpa.JPQLQuery;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

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
}
