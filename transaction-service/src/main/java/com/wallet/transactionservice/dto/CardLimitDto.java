package com.wallet.transactionservice.dto;

import java.math.BigDecimal;

public record CardLimitDto(
        BigDecimal perTransactionLimit,
        boolean limitEnabled) {
}