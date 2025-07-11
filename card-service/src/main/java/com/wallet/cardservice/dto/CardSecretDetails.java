package com.wallet.cardservice.dto;

public record CardSecretDetails(
        String number,
        String expirationDate,
        String cvv
) {
}
