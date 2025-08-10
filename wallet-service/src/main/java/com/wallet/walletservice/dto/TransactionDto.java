package com.wallet.walletservice.dto;

import com.wallet.walletservice.enums.TransactionCategory;

import java.math.BigDecimal;
import java.time.Instant;

public record TransactionDto(
        String vendor,
        TransactionCategory category,
        BigDecimal amount,
        String cardNumber,
        Instant completedAt) {
}