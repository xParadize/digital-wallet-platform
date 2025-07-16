package com.wallet.cardservice.exception;

public class CardLimitException extends RuntimeException {
    public CardLimitException(String message) {
        super(message);
    }
}
