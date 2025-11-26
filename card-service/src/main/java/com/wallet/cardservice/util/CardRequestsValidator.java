package com.wallet.cardservice.util;

import com.wallet.cardservice.enums.CardStatusAction;
import com.wallet.cardservice.exception.CardAccessDeniedException;
import com.wallet.cardservice.exception.CardLimitException;
import com.wallet.cardservice.exception.CardStatusActionException;
import com.wallet.cardservice.service.CardService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CardRequestsValidator {
    private final CardService cardService;

    @Value("${payment.limits.per-transaction-amount}")
    private BigDecimal defaultPerTransactionLimit;

    private final DecimalFormat formatter = new DecimalFormat("#,##0.00");

    public CardStatusAction validateCardStatusActionRequest(String action, Long cardId, UUID userId) {
        if (!cardService.isCardLinkedToUser(cardId, userId)) {
            throw new CardAccessDeniedException("You can't change someone's card status.");
        }
        return convertStringToCardStatusAction(action);
    }

    public void validateUpdateCardLimitRequest(Long cardId, UUID userId, BigDecimal limit) {
        if (!cardService.isCardLinkedToUser(cardId, userId)) {
            throw new CardAccessDeniedException("You can't update someone's card limit.");
        }

        if (limit.compareTo(defaultPerTransactionLimit) >= 0) {
            throw new CardLimitException("The new  limit must be less than the system default limit of " + formatter.format(defaultPerTransactionLimit));
        }
    }

    private CardStatusAction convertStringToCardStatusAction(String action) {
        try {
            return CardStatusAction.valueOf(action.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new CardStatusActionException("Invalid card action: " + action);
        }
    }

    //    public void validateRemoveCardLimitRequest(String cardNumber, UUID userId) {
//        if (!cardService.isCardLinkedToUser(cardNumber, userId)) {
//            throw new CardAccessDeniedException("You can't remove the limit on someone's card.");
//        }
//    }
//
//    public void validateSetCardLimitRequest(String cardNumber, UUID userId, Card card, BigDecimal perTransactionLimit) {
//        if (!cardService.isCardLinkedToUser(cardNumber, userId)) {
//            throw new CardAccessDeniedException("You can't set limit on someone's card.");
//        }
//
//        if (card.getLimit() != null) {
//            throw new CardLimitException("Card already has a limit set.");
//        }
//
//        if (perTransactionLimit.compareTo(defaultPerTransactionLimit) >= 0) {
//            throw new CardLimitException("The new per-transaction limit must be less than the system default limit of " + formatter.format(defaultPerTransactionLimit));
//        }
//    }
//
}
