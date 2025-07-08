package com.wallet.userservice.dto;

import java.time.LocalDateTime;

public record ApiResponse(
        boolean success,
        String message
) {
    public String getTimeStamp() {
        return LocalDateTime.now().toString();
    }
}