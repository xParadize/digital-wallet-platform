package com.wallet.cardservice.event;

import java.time.Instant;

public record CardUnfrozenEvent(
        String email,
        String cardNumber,
        Instant unfrozenAt) {
}
