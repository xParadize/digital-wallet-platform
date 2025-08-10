package com.wallet.walletservice.util;

import com.wallet.walletservice.feign.CardFeignClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CardDataValidator {
    private final CardFeignClient cardFeignClient;

    public boolean isCardExpired(String expirationDate) {
        return expired(expirationDate);
    }

    public boolean isCardLinkedToUser(String cardNumber, UUID userId) {
        return cardFeignClient.isCardLinkedToUser(cardNumber, userId);
    }

    private boolean expired(String expirationDate) {
        int month = getMonth(expirationDate.substring(0, 2));
        int year = getYear(expirationDate.substring(3, 5));

        LocalDate endDate = LocalDate.of(year, month, 1);
        return endDate.isBefore(LocalDate.now().withDayOfMonth(1));
    }

    private int getMonth(String rawMonth) {
        return Integer.parseInt(rawMonth);
    }

    private int getYear(String rawYear) {
        return Integer.parseInt("20" + rawYear);
    }
}