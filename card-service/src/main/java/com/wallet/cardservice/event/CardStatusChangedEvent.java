package com.wallet.cardservice.event;

import java.time.Instant;

public record CardStatusChangedEvent(
        String email,
        String cardNumber,
        String status,
        Instant changedAt) {
}
