package com.wallet.analyticsservice.dto;

public record CardSecretDetails(
        String number,
        String expirationDate,
        String cvv
) {
}
