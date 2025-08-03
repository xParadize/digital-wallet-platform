package com.wallet.notificationservice.event;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.Instant;

public record CardBlockedEvent(
        String email,
        String cardNumber,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        Instant blockedAt) {
}