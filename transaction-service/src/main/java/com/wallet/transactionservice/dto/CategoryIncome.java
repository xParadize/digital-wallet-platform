package com.wallet.transactionservice.dto;

import java.math.BigDecimal;

public record CategoryIncome(
        String category,
        BigDecimal income
) {
}
