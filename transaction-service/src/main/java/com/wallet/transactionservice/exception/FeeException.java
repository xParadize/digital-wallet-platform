package com.wallet.transactionservice.exception;

public class FeeException extends RuntimeException {
    public FeeException(String message) {
        super(message);
    }
}
