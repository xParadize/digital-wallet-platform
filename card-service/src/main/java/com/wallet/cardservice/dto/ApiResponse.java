package com.wallet.cardservice.dto;

import java.time.Instant;

public record ApiResponse(
        boolean success,
        String message
) {
    public String getTimeStamp() {
        return Instant.now().toString();
    }
}