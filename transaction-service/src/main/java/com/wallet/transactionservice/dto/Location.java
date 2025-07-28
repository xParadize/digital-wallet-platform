package com.wallet.transactionservice.dto;

public record Location(
        String vendor,
        float latitude,
        float longitude) {
}
