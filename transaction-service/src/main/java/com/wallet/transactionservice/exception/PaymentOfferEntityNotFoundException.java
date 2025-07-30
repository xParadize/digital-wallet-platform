package com.wallet.transactionservice.exception;

public class PaymentOfferEntityNotFoundException extends RuntimeException {
    public PaymentOfferEntityNotFoundException(String message) {
        super(message);
    }
}
