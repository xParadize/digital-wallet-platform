package com.wallet.authservice.dto;

import java.time.Instant;

public record ApiResponse (
        boolean success,
        String message
) {
    public String getTimeStamp() {
        return Instant.now().toString();
    }
}