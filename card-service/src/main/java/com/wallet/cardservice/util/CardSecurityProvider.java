package com.wallet.cardservice.util;

import org.springframework.stereotype.Component;

@Component
public class CardSecurityProvider {
    public static String maskCardNumber(String cardNumber) {
        if (cardNumber.length() <= 4) {
            return "*" + cardNumber;
        }
        String lastFour = cardNumber.substring(cardNumber.length() - 4);
        return "*" + lastFour;
    }
}
