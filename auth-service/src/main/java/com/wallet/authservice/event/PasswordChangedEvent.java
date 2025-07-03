package com.wallet.authservice.event;

public record PasswordChangedEvent(
        String email
) {
}
