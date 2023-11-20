package com.dailyon.promotionservice.domain.raffle.api;

import com.dailyon.promotionservice.domain.raffle.service.RaffleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
public class RaffleApiController {
    private final RaffleService raffleService;
}
