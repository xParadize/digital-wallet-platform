package com.wallet.notificationservice.event;

import java.time.Instant;

public record CardBlockedEvent(
        String email,
        String cardNumber,
        Instant blockedAt) {
}