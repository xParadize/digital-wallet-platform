package com.wallet.walletservice.dto;

public record CardMetadataDto(
        String issuer,
        String paymentScheme
) {
}
