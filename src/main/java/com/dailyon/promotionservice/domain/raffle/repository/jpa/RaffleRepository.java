package com.dailyon.promotionservice.domain.raffle.repository.jpa;


import com.dailyon.promotionservice.domain.raffle.entity.Raffle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

//@Repository
public interface RaffleRepository extends JpaRepository<Raffle, Long> {
}
