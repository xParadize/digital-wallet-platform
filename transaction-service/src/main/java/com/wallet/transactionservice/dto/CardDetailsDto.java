package com.wallet.transactionservice.dto;

public record CardDetailsDto(
        String number,
        String expirationDate,
        String cvv) {
}