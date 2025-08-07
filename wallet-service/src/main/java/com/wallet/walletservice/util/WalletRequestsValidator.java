package com.wallet.walletservice.util;

import com.wallet.walletservice.exception.CardAccessDeniedException;
import com.wallet.walletservice.exception.CardExpiredException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class WalletRequestsValidator {
    private final CardDataValidator cardDataValidator;

    public void validateAddCardRequest(String expirationDate, String cardNumber, UUID userId) {
        if (cardDataValidator.isCardExpired(expirationDate)) {
            throw new CardExpiredException("The card has expired");
        }

        if (cardDataValidator.isCardLinkedToUser(cardNumber, userId)) {
            throw new CardAccessDeniedException("It is not possible to add a card: it is already registered in the system");
        }
    }

    public void validateRemoveCardRequest(String cardNumber, UUID userId) {
        if (!cardDataValidator.isCardLinkedToUser(cardNumber, userId)) {
            throw new CardAccessDeniedException("You can't remove someone's card");
        }
    }
}
