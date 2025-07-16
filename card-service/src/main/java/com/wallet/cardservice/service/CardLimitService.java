package com.wallet.cardservice.service;

import com.wallet.cardservice.entity.Card;
import com.wallet.cardservice.entity.Limit;
import com.wallet.cardservice.exception.CardLimitException;
import com.wallet.cardservice.repository.CardRepository;
import com.wallet.cardservice.repository.LimitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CardLimitService {
    private final LimitRepository limitRepository;
    private final CardRepository cardRepository;

    @Transactional
    public void saveLimit(BigDecimal limitAmount, Card card) {
        Limit limit = new Limit();
        limit.setPerTransactionLimit(limitAmount);
        card.setLimit(limit);
        limitRepository.save(limit);
    }

    @Transactional
    public void updateLimit(Card card, BigDecimal newLimitAmount) {
        Limit limit = card.getLimit();
        if (limit == null)
            throw new CardLimitException("Card has no limit set");
        if (limit.getPerTransactionLimit().compareTo(newLimitAmount) == 0) {
            throw new CardLimitException("The new limit must be different from the old one");
        }

        limit.setPerTransactionLimit(newLimitAmount);
        limitRepository.save(limit);
    }
}
