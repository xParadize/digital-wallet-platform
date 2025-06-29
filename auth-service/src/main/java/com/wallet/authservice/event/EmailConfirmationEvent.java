package com.wallet.authservice.event;

import java.util.UUID;

public record EmailConfirmationEvent(
        UUID userId,
        String email,
        String name
) {
}
