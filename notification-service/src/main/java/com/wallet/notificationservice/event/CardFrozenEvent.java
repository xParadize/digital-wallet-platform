package com.wallet.notificationservice.event;

import java.time.Instant;

public record CardFrozenEvent(
        String email,
        String cardNumber,
        Instant frozenAt) {
}
