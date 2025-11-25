package com.wallet.cardservice.service;

import com.wallet.cardservice.entity.Card;
import com.wallet.cardservice.entity.Limit;
import com.wallet.cardservice.exception.CardLimitNotFoundException;
import com.wallet.cardservice.repository.LimitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class CardLimitService {

    @Value("${payment.limits.per-transaction-amount}")
    private BigDecimal defaultPerTransactionLimit;
    private final LimitRepository limitRepository;

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

//    @Transactional
//    public void saveLimit(Limit limit, Card card) {
//        card.setLimit(limit);
//        limitRepository.save(limit);
//    }
//
//    @Transactional
//    public void updateLimit(Card card, BigDecimal newLimitAmount) {
//        Limit limit = card.getLimit();
//        if (limit == null)
//            throw new CardLimitException("Card has no limit set");
//        if (limit.getPerTransactionLimit().compareTo(newLimitAmount) == 0) {
//            throw new CardLimitException("The new limit must be different from the old one");
//        }
//
//        limit.setPerTransactionLimit(newLimitAmount);
//        limitRepository.save(limit);
//    }
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
