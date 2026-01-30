package com.wallet.userservice.dto;

import java.time.Instant;

public record ApiStatusResponse(
        boolean success,
        String message
) {
    public String getTimeStamp() {
        return Instant.now().toString();
    }
}