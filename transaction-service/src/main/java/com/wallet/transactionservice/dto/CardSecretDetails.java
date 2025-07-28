package com.wallet.transactionservice.dto;

public record CardSecretDetails(
        String number,
        String expirationDate,
        String cvv
) {
}
