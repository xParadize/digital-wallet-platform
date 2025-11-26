package com.wallet.cardservice.service;

import com.wallet.cardservice.entity.Card;
import com.wallet.cardservice.entity.Limit;
import com.wallet.cardservice.exception.CardAccessDeniedException;
import com.wallet.cardservice.exception.CardLimitException;
import com.wallet.cardservice.exception.CardLimitNotFoundException;
import com.wallet.cardservice.exception.CardNotFoundException;
import com.wallet.cardservice.repository.CardRepository;
import com.wallet.cardservice.repository.LimitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LimitService {
    @Value("${payment.limits.per-transaction-amount}")
    private BigDecimal defaultPerTransactionLimit;
    private final LimitRepository limitRepository;
    private final CardRepository cardRepository;

    @Transactional(readOnly = true)
    public Limit getLimitByCard(Card card) {
        return limitRepository.findByCard(card)
                .orElseThrow(CardLimitNotFoundException::new);
    }

    @Transactional(readOnly = true)
    public Limit createDefaultLimit() {
        return Limit.builder()
                .limitAmount(defaultPerTransactionLimit)
                .build();
    }

    @CacheEvict(value = "card", key = "#cardId + ':user:' + #userId")
    @Transactional
    public void updateLimit(Long cardId, UUID userId, BigDecimal newLimit) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException("Card not found"));
        if (!card.getUserId().equals(userId)) {
            throw new CardAccessDeniedException("Access to the card is forbidden");
        }

        Limit oldLimit = card.getLimit();
        if (oldLimit == null) {
            throw new CardLimitException("Card has no limit set");
        }
        if (oldLimit.getLimitAmount().compareTo(newLimit) == 0) {
            throw new CardLimitException("The new limit must be different from the old one");
        }

        oldLimit.setLimitAmount(newLimit);
        limitRepository.save(oldLimit);
    }

//    @Transactional
//    public void saveLimit(Limit limit, Card card) {
//        card.setLimit(limit);
//        limitRepository.save(limit);
//    }
//
//
//    @Transactional
//    public void removeLimit(Card card) {
//        Limit limit = card.getLimit();
//        if (limit == null)
//            throw new CardLimitException("Card has no limit set");
//
//        card.setLimit(createDefaultLimit(card));
//        limitRepository.delete(limit);
//        cardRepository.save(card);
//    }
//
//    private Limit createDefaultLimit(Card card) {
//        return Limit.builder()
//                .perTransactionLimit(defaultPerTransactionLimit)
//                .limitEnabled(true)
//                .card(card)
//                .build();
//    }
}
