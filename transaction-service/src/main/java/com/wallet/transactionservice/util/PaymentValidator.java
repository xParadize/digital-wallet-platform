package com.wallet.transactionservice.util;

import com.wallet.transactionservice.dto.CardDetailsDto;
import com.wallet.transactionservice.dto.PaymentOffer;
import com.wallet.transactionservice.dto.PaymentRequestDto;
import com.wallet.transactionservice.exception.CardAccessDeniedException;
import com.wallet.transactionservice.exception.CardNotFoundException;
import com.wallet.transactionservice.exception.InsufficientBalanceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PaymentValidator {
    private final CardDataValidator cardDataValidator;

    public void validatePayment(PaymentRequestDto paymentRequest, CardDetailsDto cardDetails,
                                UUID userId, PaymentOffer paymentOffer) {
        validateCardExists(cardDetails);
        validateCardOwnership(cardDetails, userId);
        validateCardCredentials(paymentRequest, cardDetails);
        validateCardStatus(cardDetails);
        validateBalance(cardDetails, paymentOffer);
    }

    private void validateCardExists(CardDetailsDto cardDetails) {
        if (cardDetails == null) {
            throw new CardNotFoundException("Card not found");
        }
    }

    public void validateCardOwnership(CardDetailsDto cardDetails, UUID userId) {
        if (!cardDetails.getHolder().id().equals(userId)) {
            throw new CardAccessDeniedException("Not your card");
        }
    }

    private void validateCardCredentials(PaymentRequestDto paymentRequest, CardDetailsDto cardDetails) {
        if (!paymentRequest.getCvv().equals(cardDetails.getSecretDetails().cvv())) {
            throw new CardAccessDeniedException("Incorrect CVV");
        }
    }

    private void validateCardStatus(CardDetailsDto cardDetails) {
        if (cardDetails.isFrozen()) {
            throw new CardAccessDeniedException("The card is frozen");
        }
        if (cardDetails.isBlocked()) {
            throw new CardAccessDeniedException("The card is blocked");
        }
        if (cardDataValidator.isCardExpired(cardDetails.getSecretDetails().expirationDate())) {
            throw new CardAccessDeniedException("The card is expired");
        }
    }

    private void validateBalance(CardDetailsDto cardDetails, PaymentOffer paymentOffer) {
        if (cardDetails.getBalance().compareTo(paymentOffer.amount().value()) < 0) {
            throw new InsufficientBalanceException("Insufficient balance");
        }
    }
}
