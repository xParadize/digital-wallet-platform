package com.wallet.cardservice.repository;

import com.wallet.cardservice.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {
    Optional<Card> getCardByNumber(String number);
    List<Card> findAllByUserId(UUID userId);
    void deleteCardByNumber(String number);
}
