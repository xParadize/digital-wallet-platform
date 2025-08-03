package com.wallet.transactionservice.util;

import com.wallet.transactionservice.entity.Transaction;
import com.wallet.transactionservice.exception.IncorrectTimePeriodException;
import com.wallet.transactionservice.exception.TransactionNotFoundException;
import com.wallet.transactionservice.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;

@Component
@RequiredArgsConstructor
public class LocalDateValidator {
    private final TransactionRepository transactionRepository;
    private final ZoneId TIMESTAMP_ZONE = ZoneOffset.UTC;

    public void validate(String cardNumber, LocalDate from, LocalDate to) {
        if (!isBeforeOrEqual(from, to) || !isAfterFirstTransaction(from, cardNumber)) {
            throw new IncorrectTimePeriodException("You can't select this date");
        }
    }

    private boolean isBeforeOrEqual(LocalDate date1, LocalDate date2) {
        return !date1.isAfter(date2);
    }

    private boolean isAfterFirstTransaction(LocalDate date, String cardNumber) {
        LocalDate firstTransactionDate = getFirstTransactionTimestampByCardNumber(cardNumber)
                .atZone(TIMESTAMP_ZONE).toLocalDate();
        return isAfterOrEqual(date, firstTransactionDate);
    }

    private boolean isAfterOrEqual(LocalDate date1, LocalDate date2) {
        return !date1.isBefore(date2);
    }

    private Instant getFirstTransactionTimestampByCardNumber(String number) {
        Transaction firstTransaction = transactionRepository.findFirstByCardNumberOrderByCreatedAtAsc(number)
                .orElseThrow(() -> new TransactionNotFoundException("We couldn't find any transactions for this card. It may not have been used yet."));
        return firstTransaction.getConfirmedAt();
    }
}
