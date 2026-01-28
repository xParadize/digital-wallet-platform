package com.wallet.transactionservice.dto;

public record CardMetadataDto(
        String issuer,
        String paymentScheme) {
}
