package com.wallet.transactionservice.repository;

import com.wallet.transactionservice.entity.Transaction;
import com.wallet.transactionservice.enums.TransactionStatus;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    Optional<Transaction> getTransactionById(UUID id);
    Optional<Transaction> findByUserIdAndOfferIdAndStatus(UUID userId, String offerId, TransactionStatus status);
    List<Transaction> findAllByCardNumberAndConfirmedAtBetween(String cardNumber, Instant confirmedAtAfter, Instant confirmedAtBefore);
    List<Transaction> findAllByCardNumberAndConfirmedAtBetween(String cardNumber, Instant confirmedAtAfter, Instant confirmedAtBefore, Pageable pageable);
    List<Transaction> findAllByCardNumberAndConfirmedAtBetweenAndAmountLessThan(String cardNumber, Instant confirmedAtAfter, Instant confirmedAtBefore, BigDecimal amountIsLessThan, Pageable pageable);
    List<Transaction> findAllByCardNumberAndConfirmedAtBetweenAndAmountGreaterThan(String cardNumber, Instant confirmedAtAfter, Instant confirmedAtBefore, BigDecimal amountIsGreaterThan, Pageable pageable);
    Optional<Transaction> findFirstByCardNumberOrderByCreatedAtAsc(String cardNumber);
    List<Transaction> findByCardNumberOrderByConfirmedAtDesc(String cardNumber, Limit limit);
    List<Transaction> findAllByUserIdOrderByConfirmedAtDesc(UUID userId, Pageable pageable);
}
