package com.wallet.cardservice.repository;

import com.wallet.cardservice.entity.Card;
import com.wallet.cardservice.entity.CardMetadata;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {

    @EntityGraph(attributePaths = {"cardMetadata", "cardDetails", "limit"})
    List<Card> findByCardDetails_NumberIn(List<String> numbers);

    @EntityGraph(attributePaths = {"cardMetadata", "cardDetails", "limit"})
    List<Card> findAllByUserIdOrderByCardMetadata_IssuerDesc(UUID userId, Pageable pageable);

    @EntityGraph(attributePaths = {"cardMetadata", "cardDetails", "limit"})
    List<Card> findAllByUserIdOrderByCardMetadata_IssuerAsc(UUID userId, Pageable pageable);

    @EntityGraph(attributePaths = {"cardMetadata", "cardDetails", "limit"})
    List<Card> findAllByUserIdOrderByBalanceDesc(UUID userId, Pageable pageable);

    @EntityGraph(attributePaths = {"cardMetadata", "cardDetails", "limit"})
    List<Card> findAllByUserIdOrderByBalanceAsc(UUID userId, Pageable pageable);

    @Query("SELECT c FROM Card c " +
            "LEFT JOIN FETCH c.cardDetails cd " +
            "LEFT JOIN FETCH c.cardMetadata cm " +
            "LEFT JOIN FETCH c.limit l " +
            "WHERE cd.number = :cardNumber AND c.userId = :userId")
    Optional<Card> findByCardNumberAndUserIdWithDetails(
            @Param("cardNumber") String cardNumber,
            @Param("userId") UUID userId
    );

    int countByUserId(UUID userId);

    @EntityGraph(attributePaths = {"cardMetadata", "cardDetails", "limit"})
    Optional<Card> findByCardDetails_Number(String cardNumber);

    @Query("SELECT c FROM Card c " +
            "JOIN FETCH c.cardDetails cd " +
            "JOIN FETCH c.cardMetadata cm " +
            "JOIN FETCH c.limit " +
            "WHERE c.userId = :userId " +
            "ORDER BY FUNCTION('TO_DATE', CONCAT(cd.expirationDate, '01'), 'MM/YY/DD') ASC")
    List<Card> findByUserIdOrderByExpirationDateEarliest(@Param("userId") UUID userId, Pageable pageable);

    @Query("SELECT c FROM Card c " +
            "JOIN FETCH c.cardDetails cd " +
            "JOIN FETCH c.cardMetadata cm " +
            "JOIN FETCH c.limit " +
            "WHERE c.userId = :userId " +
            "ORDER BY FUNCTION('TO_DATE', CONCAT(cd.expirationDate, '01'), 'MM/YY/DD') DESC")
    List<Card> findByUserIdOrderByExpirationDateLatest(@Param("userId") UUID userId, Pageable pageable);

    @Query("SELECT c FROM Card c " +
            "JOIN FETCH c.cardDetails " +
            "JOIN FETCH c.cardMetadata " +
            "JOIN FETCH c.limit l " +
            "WHERE c.userId = :userId " +
            "ORDER BY l.limitAmount DESC")
    List<Card> findByUserIdOrderByLimitValueDesc(@Param("userId") UUID userId, Pageable pageable);

    @Query("SELECT c FROM Card c " +
            "JOIN FETCH c.cardDetails " +
            "JOIN FETCH c.cardMetadata " +
            "JOIN FETCH c.limit l " +
            "WHERE c.userId = :userId " +
            "ORDER BY l.limitAmount ASC")
    List<Card> findByUserIdOrderByLimitValueAsc(@Param("userId") UUID userId, Pageable pageable);
}
