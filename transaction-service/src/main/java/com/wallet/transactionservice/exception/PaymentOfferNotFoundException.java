package com.wallet.transactionservice.exception;

public class PaymentOfferNotFoundException extends RuntimeException {
    public PaymentOfferNotFoundException(String message) {
        super(message);
    }
}
