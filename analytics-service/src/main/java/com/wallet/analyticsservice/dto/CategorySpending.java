package com.wallet.analyticsservice.dto;

import java.math.BigDecimal;

public record CategorySpending(
        String category,
        BigDecimal spending
) {
}