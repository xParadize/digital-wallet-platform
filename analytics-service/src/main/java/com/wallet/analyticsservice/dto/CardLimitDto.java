package com.wallet.analyticsservice.dto;

import java.math.BigDecimal;

public record CardLimitDto(
        BigDecimal perTransactionLimit,
        boolean limitEnabled) {
}