package com.wallet.walletservice.dto;

import java.math.BigDecimal;

public record Limit(
        BigDecimal perTransactionLimit
) {
}
