package com.wallet.cardservice.repository;

import com.wallet.cardservice.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {
    Optional<Card> getCardByNumber(String number);
    List<Card> findAllByUserId(UUID userId);
    List<Card> findByUserIdOrderByMoneyDesc(UUID userId);
    List<Card> findByUserIdOrderByMoneyAsc(UUID userId);
    List<Card> findByUserIdOrderByCardIssuerDesc(UUID userId);
    List<Card> findByUserIdOrderByCardIssuerAsc(UUID userId);

    @Query(value = "SELECT c.* FROM card_ c " +
            "JOIN limit_ l ON c.limit_id = l.id " +
            "WHERE c.user_id = :userId " +
            "ORDER BY l.per_transaction_limit DESC",
            nativeQuery = true)
    List<Card> findByUserIdOrderByLimitValueDesc(@Param("userId") UUID userId);

    @Query(value = "SELECT c.* FROM card_ c " +
            "JOIN limit_ l ON c.limit_id = l.id " +
            "WHERE c.user_id = :userId " +
            "ORDER BY l.per_transaction_limit ASC",
        nativeQuery = true)
    List<Card> findByUserIdOrderByLimitValueAsc(@Param("userId") UUID userId);

    @Query(value = "SELECT * FROM card_ " +
            "ORDER BY (TO_DATE(expiration_date || '/01', 'MM/YY/DD') + INTERVAL '1 month - 1 day') ASC",
            nativeQuery = true)
    List<Card> findByUserIdOrderByExpirationDateEarliest(UUID userId);

    @Query(value = "SELECT * FROM card_ " +
            "ORDER BY (TO_DATE(expiration_date || '/01', 'MM/YY/DD') + INTERVAL '1 month - 1 day') DESC",
            nativeQuery = true)
    List<Card> findByUserIdOrderByExpirationDateLatest(UUID userId);

    void deleteCardByNumber(String number);
}
