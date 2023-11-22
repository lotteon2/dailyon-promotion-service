package com.dailyon.promotionservice.domain.raffle.repository.dynamodb;

import com.dailyon.promotionservice.domain.raffle.entity.dynamodb.RaffleEntryHistory;

import org.springframework.data.repository.CrudRepository;


public interface RaffleEntryHistoryRepository extends CrudRepository<RaffleEntryHistory, Long> {
    // Define custom query methods here
}