package com.wallet.cardservice.service;

import com.wallet.cardservice.dto.LimitDto;
import com.wallet.cardservice.entity.Card;
import com.wallet.cardservice.entity.Limit;
import com.wallet.cardservice.exception.CardAccessDeniedException;
import com.wallet.cardservice.exception.CardLimitException;
import com.wallet.cardservice.exception.CardLimitNotFoundException;
import com.wallet.cardservice.exception.CardNotFoundException;
import com.wallet.cardservice.mapper.LimitMapper;
import com.wallet.cardservice.repository.CardRepository;
import com.wallet.cardservice.repository.LimitRepository;
import com.wallet.cardservice.util.CardSecurityProvider;
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
    private final LimitMapper limitMapper;
    private final CardSecurityProvider cardSecurityProvider;

    @Transactional(readOnly = true)
    public Limit getLimitByCard(Long cardId) {
        return limitRepository.findByCard_Id(cardId)
                .orElseThrow(CardLimitNotFoundException::new);
    }

    @Transactional(readOnly = true)
    public Limit createDefaultLimit() {
        return Limit.builder()
                .limitAmount(defaultPerTransactionLimit)
                .build();
    }

    @Transactional(readOnly = true)
    public LimitDto getLimitDto(Long cardId, UUID userId) {
        cardSecurityProvider.checkCardOwner(cardId, userId);
        return limitMapper.toDto(getLimitByCard(cardId));
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
}
