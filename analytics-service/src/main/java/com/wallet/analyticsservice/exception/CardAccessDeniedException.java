package com.wallet.analyticsservice.exception;

public class CardAccessDeniedException extends RuntimeException {
    public CardAccessDeniedException(String message) {
        super(message);
    }
}
