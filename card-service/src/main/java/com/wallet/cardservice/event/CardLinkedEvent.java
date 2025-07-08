package com.wallet.cardservice.event;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.UUID;

public record CardLinkedEvent(
        UUID userId,
        String email,
        String cardNumber,
        String cardIssuer,
        String cardScheme,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime linkedAt
) {
}
