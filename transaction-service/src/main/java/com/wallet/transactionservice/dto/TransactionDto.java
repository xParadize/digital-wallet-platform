package com.wallet.transactionservice.dto;

import com.wallet.transactionservice.enums.TransactionCategory;

import java.math.BigDecimal;

public record TransactionDto(
        String vendor,
        TransactionCategory category,
        BigDecimal amount,
        String cardNumber,
        String confirmedAt,
        String comment) {
}
