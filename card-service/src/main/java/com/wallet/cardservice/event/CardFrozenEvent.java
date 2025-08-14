package com.wallet.cardservice.event;

import java.time.Instant;

public record CardFrozenEvent(
        String email,
        String cardNumber,
        Instant frozenAt) {
}
