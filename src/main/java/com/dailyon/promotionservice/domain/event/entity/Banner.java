package com.dailyon.promotionservice.domain.event.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Banner {

    @Id
    @Column(nullable = false)
    private Long eventPageId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String imgUrl;

    @Column(nullable = false)
    private String linkUrl;

    @Column(nullable = false)
    private LocalDateTime startAt;

    @Column(nullable = false)
    private LocalDateTime endAt;

    // Assuming there is a one-to-one relationship between EventPage and Banner
    @OneToOne
    @JoinColumn(name = "event_page_id", referencedColumnName = "id", insertable = false, updatable = false)
    private EventPage eventPage;

    // Getters and setters ...
}