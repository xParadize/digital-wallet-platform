package com.wallet.cardservice.dto;

import java.math.BigDecimal;

public record CardLimitDto(
        BigDecimal perTransactionLimit,
        boolean limitEnabled) {
}
