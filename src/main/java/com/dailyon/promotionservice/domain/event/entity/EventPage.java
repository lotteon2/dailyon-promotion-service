package com.dailyon.promotionservice.domain.event.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
public class EventPage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private LocalDateTime startAt;

    @Column(nullable = false)
    private LocalDateTime endAt;

    private String description;

    private String backgroundImgUrl;


}