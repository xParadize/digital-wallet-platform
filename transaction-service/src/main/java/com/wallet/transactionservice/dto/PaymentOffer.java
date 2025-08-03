package com.wallet.transactionservice.dto;

import java.time.Instant;

public record PaymentOffer(
        String id,
        Amount amount,
        String category,
        Location location,
        Instant suggestedAt
) {
}
