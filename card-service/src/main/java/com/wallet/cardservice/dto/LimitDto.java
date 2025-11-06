package com.wallet.cardservice.dto;

import java.math.BigDecimal;

public record LimitDto(
        BigDecimal limitAmount
) {
}
