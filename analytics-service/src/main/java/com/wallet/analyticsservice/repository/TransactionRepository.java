package com.wallet.analyticsservice.repository;

import com.wallet.analyticsservice.entity.TransactionEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TransactionRepository {
    private final JdbcTemplate jdbcTemplate;

    public void save(TransactionEvent transactionEvent) {
        String sql = "INSERT INTO transaction_events (id, user_id, offer_id, status, card_type, created_at, confirmed_at, cancelled_at, card_number, amount, transaction_type) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql,
                transactionEvent.getId(),
                transactionEvent.getUserId(),
                transactionEvent.getOfferId(),
                transactionEvent.getStatus(),
                transactionEvent.getCardType(),
                transactionEvent.getCreatedAt(),
                transactionEvent.getConfirmedAt(),
                transactionEvent.getCancelledAt(),
                transactionEvent.getCardNumber(),
                transactionEvent.getAmount(),
                transactionEvent.getTransactionType()
        );
    }
}
