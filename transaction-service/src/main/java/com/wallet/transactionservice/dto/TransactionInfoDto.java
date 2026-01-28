package com.wallet.transactionservice.dto;

import com.wallet.transactionservice.enums.TransactionCategory;

import java.math.BigDecimal;
import java.time.Instant;

public record TransactionInfoDto(
        String vendor,
        TransactionCategory category,
        BigDecimal amount,
        BigDecimal commission,
        String cardNumber,
        Instant completedAt) {
}
