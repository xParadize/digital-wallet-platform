package com.wallet.transactionservice.dto;

import java.math.BigDecimal;

public record Amount(
        BigDecimal value,
        String currency) {
}
