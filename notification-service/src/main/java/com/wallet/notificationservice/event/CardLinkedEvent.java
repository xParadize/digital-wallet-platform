package com.wallet.notificationservice.event;

import java.time.Instant;
import java.util.UUID;

public record CardLinkedEvent(
        UUID userId,
        String email,
        String cardNumber,
        String cardIssuer,
        String cardScheme,
        Instant linkedAt
) {
}