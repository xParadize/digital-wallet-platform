package com.wallet.cardservice.util;

import com.wallet.cardservice.enums.CardStatusAction;
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
    private final CardSecurityProvider cardSecurityProvider;

    @Value("${payment.limits.per-transaction-amount}")
    private BigDecimal defaultPerTransactionLimit;

    private final DecimalFormat formatter = new DecimalFormat("#,##0.00");

    public CardStatusAction validateCardStatusActionRequest(String action, Long cardId, UUID userId) {
        cardSecurityProvider.checkCardOwner(cardId, userId);
        return convertStringToCardStatusAction(action);
    }

    public void validateUpdateCardLimitRequest(Long cardId, UUID userId, BigDecimal limit) {
        cardSecurityProvider.checkCardOwner(cardId, userId);
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
}
