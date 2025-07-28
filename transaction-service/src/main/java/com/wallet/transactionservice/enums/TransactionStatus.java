package com.wallet.transactionservice.enums;

public enum TransactionStatus {
    PENDING,       // Ожидает подтверждения OTP
    CONFIRMED,     // Успешно оплачено
    CANCELLED,     // Отменена юзером или системой
    FAILED         // Ошибка списания
}
