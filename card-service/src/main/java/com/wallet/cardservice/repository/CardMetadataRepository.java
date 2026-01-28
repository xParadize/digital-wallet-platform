package com.wallet.cardservice.repository;

import com.wallet.cardservice.entity.Card;
import com.wallet.cardservice.entity.CardMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CardMetadataRepository extends JpaRepository<CardMetadata, Long> {
    Optional<CardMetadata> findByCard(Card card);
}
