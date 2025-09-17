package com.wallet.walletservice.util;

import com.wallet.walletservice.dto.CardSort;
import com.wallet.walletservice.enums.CardSortOrder;
import com.wallet.walletservice.enums.CardSortType;
import com.wallet.walletservice.exception.CardAccessDeniedException;
import com.wallet.walletservice.exception.CardExpiredException;
import com.wallet.walletservice.exception.RequestedSortException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class WalletRequestsValidator {
    private final CardDataValidator cardDataValidator;
    private final CardSortValidator cardSortValidator;

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

    public CardSort validateGetLinkedCardsRequest(String sort, String order) {
        return cardSortValidator.validateSort(sort, order);
    }
}
