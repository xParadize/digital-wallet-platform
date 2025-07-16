package com.wallet.cardservice.service;

import com.wallet.cardservice.entity.Card;
import com.wallet.cardservice.entity.Limit;
import com.wallet.cardservice.repository.LimitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class CardLimitService {
    private final LimitRepository limitRepository;

    @Transactional
    public void saveLimit(BigDecimal limitAmount, Card card) {
        Limit limit = new Limit();
        limit.setPerTransactionLimit(limitAmount);
        card.setLimit(limit);
        limitRepository.save(limit);
    }
}
