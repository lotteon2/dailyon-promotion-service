package com.dailyon.promotionservice.domain.raffle.entity;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Slf4j
@Getter
@Entity
public class Raffle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long raffleProductId;

    @Column(nullable = false)
    private Long productSizeId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String raffleImgUrl;

    @Column(nullable = false)
    private LocalDateTime startAt;

    @Column(nullable = false)
    private LocalDateTime endAt;

    @Column(nullable = false)
    private Long quantity;

    @Column(nullable = false)
    private Long regularPrice;

    @Column(nullable = false)
    private Long promotionalPrice;

}
