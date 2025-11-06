package com.wallet.cardservice.repository;

import com.wallet.cardservice.entity.Card;
import com.wallet.cardservice.entity.CardDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CardDetailsRepository extends JpaRepository<CardDetails, Long> {
    Optional<CardDetails> findByCard(Card card);
}
