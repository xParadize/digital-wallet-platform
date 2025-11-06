package com.wallet.cardservice.dto;

public record CardDetailsDto(
        String number,
        String expirationDate,
        String cvv
) {
}
