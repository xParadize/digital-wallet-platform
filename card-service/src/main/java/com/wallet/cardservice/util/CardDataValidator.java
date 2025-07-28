package com.wallet.cardservice.util;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public class CardDataValidator {
    public boolean isCardExpired(String expirationDate) {
        return expired(expirationDate);
    }

    private boolean expired(String expirationDate) {
        YearMonth expiry = YearMonth.parse(expirationDate, DateTimeFormatter.ofPattern("MM/yy"));
        return expiry.isBefore(YearMonth.now());
    }
}