package com.wallet.transactionservice.exception;

public class IncorrectTimePeriodException extends RuntimeException {
    public IncorrectTimePeriodException(String message) {
        super(message);
    }
}
