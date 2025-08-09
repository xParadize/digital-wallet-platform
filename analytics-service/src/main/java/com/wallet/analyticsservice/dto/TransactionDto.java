package com.wallet.analyticsservice.dto;

import com.wallet.analyticsservice.enums.TransactionCategory;

import java.math.BigDecimal;
import java.time.Instant;

public record TransactionDto(
        String vendor,
        TransactionCategory category,
        BigDecimal amount,
        String cardNumber,
        Instant completedAt) {
}