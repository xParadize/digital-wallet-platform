package com.wallet.transactionservice.dto;

public record PaymentRequestDto(
        Long cardId,
        String cvv
) {
}
