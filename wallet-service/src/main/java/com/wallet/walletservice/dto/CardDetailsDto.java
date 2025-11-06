package com.wallet.walletservice.dto;

public record CardDetailsDto(
        String number,
        String expirationDate,
        String cvv
) {
}
