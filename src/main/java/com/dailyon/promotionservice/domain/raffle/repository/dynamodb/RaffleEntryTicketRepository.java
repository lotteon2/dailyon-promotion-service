package com.dailyon.promotionservice.domain.raffle.repository.dynamodb;


import com.dailyon.promotionservice.domain.raffle.entity.dynamodb.RaffleEntryTicket;
import org.springframework.data.repository.CrudRepository;


public interface RaffleEntryTicketRepository extends CrudRepository<RaffleEntryTicket, String> {
}