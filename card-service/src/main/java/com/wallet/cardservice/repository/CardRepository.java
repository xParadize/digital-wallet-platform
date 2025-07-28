package com.wallet.cardservice.repository;

import com.wallet.cardservice.entity.Card;
import com.wallet.cardservice.entity.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {
    Optional<Card> getCardByNumber(String number);
    List<Card> findAllByUserId(UUID userId);
    void deleteCardByNumber(String number);
}
