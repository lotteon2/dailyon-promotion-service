package com.dailyon.promotionservice.domain.raffle.service;

import com.dailyon.promotionservice.domain.raffle.repository.jpa.RaffleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class RaffleService {
    private final RaffleRepository raffleRepository;
}
