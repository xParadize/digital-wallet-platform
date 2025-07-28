package com.wallet.transactionservice.dto;

public record PaymentResult(
        boolean requiresOtp,
        String message
) {
    public static PaymentResult success() {
        return new PaymentResult(false, null);
    }
    public static PaymentResult requiresOtp(String message) {
        return new PaymentResult(true, message);
    }
}