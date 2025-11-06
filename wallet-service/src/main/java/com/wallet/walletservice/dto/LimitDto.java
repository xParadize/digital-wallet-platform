package com.wallet.walletservice.dto;

import java.math.BigDecimal;

public record LimitDto(
        BigDecimal limitAmount
) {
}
