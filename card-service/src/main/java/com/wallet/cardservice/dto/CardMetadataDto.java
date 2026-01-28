package com.wallet.cardservice.dto;

public record CardMetadataDto(
        String issuer,
        String paymentScheme
) {
}
