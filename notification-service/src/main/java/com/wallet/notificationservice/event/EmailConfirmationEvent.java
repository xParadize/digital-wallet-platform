package com.wallet.notificationservice.event;

import java.util.UUID;

public record EmailConfirmationEvent(
        UUID userId,
        String email,
        String name
) {
}
