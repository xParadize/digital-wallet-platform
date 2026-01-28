package com.wallet.transactionservice.exception;

public class PaymentFailedException extends RuntimeException {
    public PaymentFailedException(String message, Exception e) {
        super(message, e);
    }
}
