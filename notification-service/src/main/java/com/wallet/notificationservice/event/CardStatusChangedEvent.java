package com.wallet.notificationservice.event;

import java.time.Instant;

public record CardStatusChangedEvent(
        String email,
        String cardNumber,
        String status,
        Instant changedAt) {
}