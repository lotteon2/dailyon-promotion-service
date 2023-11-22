package com.dailyon.promotionservice.domain.raffle.entity;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Slf4j
@Getter
@Entity
public class Raffle {
    @Id
    private Long id;

    private Long raffleProductId;
    private Long productSizeId;
    private String title;
    private String raffleImgUrl;

    private LocalDateTime startAt;
    private LocalDateTime endAt;

    private Long quantity;
    private Long regularPrice;
    private Long promotionalPrice;

}
