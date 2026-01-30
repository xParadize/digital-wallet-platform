package com.wallet.cardservice.util;

import com.wallet.cardservice.exception.CardAccessDeniedException;
import com.wallet.cardservice.exception.CardAmountException;
import com.wallet.cardservice.repository.CardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CardSecurityProvider {
    private final CardRepository cardRepository;

    @Value("${card.max-active-cards-per-user}")
    private int maxUserCardsAmount;

    @Transactional(readOnly = true)
    public String maskCardNumber(String cardNumber) {
        if (cardNumber.length() <= 4) {
            return "*" + cardNumber;
        }
        String lastFour = cardNumber.substring(cardNumber.length() - 4);
        return "*" + lastFour;
    }

    @Transactional(readOnly = true)
    public void checkCardOwner(Long cardId, UUID userId) {
        boolean existsByUserId = cardRepository.findById(cardId)
                .map(card -> card.getUserId().equals(userId))
                .orElse(false);
        if (!existsByUserId) {
            throw new CardAccessDeniedException("Access to the card is forbidden");
        }
    }

    @Transactional(readOnly = true)
    public void checkUserCardAmount(UUID userId) {
        int currentCardCount = cardRepository.countByUserId(userId);
        if (currentCardCount >= maxUserCardsAmount) {
            throw new CardAmountException("You have reached the maximum number of active cards allowed: " + maxUserCardsAmount);
        }
    }
}
