package com.wallet.cardservice.event;

import java.time.Instant;

public record CardBlockedEvent(
        String email,
        String cardNumber,
        Instant blockedAt) {
}
