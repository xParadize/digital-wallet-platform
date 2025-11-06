package com.wallet.walletservice.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record CardDto(
        UUID userId,
        BigDecimal balance,
        String status) {
}
