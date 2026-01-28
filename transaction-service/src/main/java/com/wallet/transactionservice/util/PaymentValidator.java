package com.wallet.transactionservice.util;

import com.wallet.transactionservice.dto.CardDetailsDto;
import com.wallet.transactionservice.dto.CardInfoDto;
import com.wallet.transactionservice.dto.PaymentOffer;
import com.wallet.transactionservice.dto.PaymentRequestDto;
import com.wallet.transactionservice.enums.CardStatus;
import com.wallet.transactionservice.exception.CardAccessDeniedException;
import com.wallet.transactionservice.exception.CardNotFoundException;
import com.wallet.transactionservice.exception.InsufficientBalanceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentValidator {
    private final CardDataValidator cardDataValidator;

    public void validatePayment(PaymentRequestDto paymentRequest, CardInfoDto cardInfoDto, UUID userId, PaymentOffer paymentOffer) {
        validateCardExists(cardInfoDto);
        validateCardOwnership(cardInfoDto, userId);
        validateCardCredentials(paymentRequest, cardInfoDto);
        validateCardStatus(cardInfoDto);
        validateBalance(cardInfoDto, paymentOffer);
    }

    private void validateCardExists(CardInfoDto cardInfoDto) {
        if (cardInfoDto == null) {
            throw new CardNotFoundException("Card not found");
        }
    }

    public void validateCardOwnership(CardInfoDto cardInfoDto, UUID userId) {
        if (cardInfoDto.getHolder() == null || !cardInfoDto.getHolder().id().equals(userId)) {
            throw new CardAccessDeniedException("Payment authorization failed");
        }
    }

    private void validateCardCredentials(PaymentRequestDto paymentRequest, CardInfoDto cardInfoDto) {
        if (cardInfoDto.getSecretDetails() == null) {
            throw new CardAccessDeniedException("Payment authorization failed");
        }

        String requestCvv = paymentRequest.getCvv();
        String cardCvv = cardInfoDto.getSecretDetails().cvv();

        if (requestCvv == null || !Objects.equals(requestCvv, cardCvv)) {
            throw new CardAccessDeniedException("Payment authorization failed");
        }

        if (cardDataValidator.isCardExpired(cardInfoDto.getSecretDetails().expirationDate())) {
            throw new CardAccessDeniedException("Card has expired");
        }
    }

    private void validateCardStatus(CardInfoDto cardInfoDto) {
        if (cardInfoDto.getCardDto() == null) {
            throw new CardAccessDeniedException("Payment authorization failed");
        }

        CardStatus status = CardStatus.valueOf(cardInfoDto.getCardDto().status());
        switch (status) {
            case BLOCKED -> throw new CardAccessDeniedException("Card is blocked");
            case FROZEN -> throw new CardAccessDeniedException("Card is inactive");
            case ACTIVE -> {}
            default -> throw new CardAccessDeniedException("Unknown card status");
        }
    }

    private void validateBalance(CardInfoDto cardInfoDto, PaymentOffer paymentOffer) {
        if (cardInfoDto.getCardDto() == null || cardInfoDto.getCardDto().balance().compareTo(paymentOffer.amount().value()) < 0) {
            throw new InsufficientBalanceException("Insufficient balance");
        }
    }
}
