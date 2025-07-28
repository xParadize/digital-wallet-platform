package com.wallet.transactionservice.dto;

import java.time.LocalDateTime;

public record PaymentOffer(
        String id,
        Amount amount,
        String category,
        Location location,
        LocalDateTime suggestedAt
) {
}
