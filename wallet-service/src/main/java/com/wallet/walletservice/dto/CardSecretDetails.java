package com.wallet.walletservice.dto;

public record CardSecretDetails(
        String number,
        String expirationDate,
        String cvv
) {
}
