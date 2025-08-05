package com.wallet.transactionservice.dto;

import java.math.BigDecimal;

public record CategorySpending(
        String category,
        BigDecimal spending
) {
}
