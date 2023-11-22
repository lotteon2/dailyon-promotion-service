package com.dailyon.promotionservice.domain.event.entity;

import com.dailyon.promotionservice.domain.coupon.entity.CouponInfo;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Entity
public class EventPageCoupon {

    @EmbeddedId
    private EventPageCouponId id;

    @MapsId("eventPageId") // Matches the field in the composite key class
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_page_id")
    private EventPage eventPage;

    @MapsId("couponInfoId") // Matches the field in the composite key class
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_info_id")
    private CouponInfo couponInfo;

    // Inner class for the composite key
    @Embeddable
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class EventPageCouponId implements Serializable {
        @Column(name = "event_page_id")
        private Long eventPageId;

        @Column(name = "coupon_info_id")
        private Long couponInfoId;
    }
    // Other entity details ...
}