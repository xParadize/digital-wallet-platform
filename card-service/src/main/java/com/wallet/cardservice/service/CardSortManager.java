package com.wallet.cardservice.service;

import com.wallet.cardservice.entity.Card;
import com.wallet.cardservice.enums.CardSortOrder;
import com.wallet.cardservice.feign.TransactionFeignClient;
import com.wallet.cardservice.repository.CardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CardSortManager {
    private final CardRepository cardRepository;
    private final TransactionFeignClient transactionFeignClient;

    @Transactional(readOnly = true)
    public List<Card> findAllCardsByLastUse(UUID userId, int offset, int limit) {
        List<String> lastUsedCardNumbers = transactionFeignClient.getLastUsedCardNumbers(userId, offset, limit);
        if (lastUsedCardNumbers.isEmpty()) {
            return Collections.emptyList();
        }
        return lastUsedCardNumbers.stream()
                .map(cardRepository::findByCardDetails_Number)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Card> findAllCardsByIssuerName(UUID userId, CardSortOrder order, Pageable pageable) {
        switch (order) {
            case DALPH -> {
                return cardRepository.findAllByUserIdOrderByCardMetadata_IssuerDesc(userId, pageable);
            } case AALPH -> {
                return cardRepository.findAllByUserIdOrderByCardMetadata_IssuerAsc(userId, pageable);
            } default -> {
                return List.of();
            }
        }
    }

    @Transactional(readOnly = true)
    public List<Card> findAllCardsByBalance(UUID userId, CardSortOrder order, Pageable pageable) {
        switch (order) {
            case DESC -> {
                return cardRepository.findAllByUserIdOrderByBalanceDesc(userId, pageable);
            } case ASC -> {
                return cardRepository.findAllByUserIdOrderByBalanceAsc(userId, pageable);
            } default -> {
                return List.of();
            }
        }
    }

    @Transactional(readOnly = true)
    public List<Card> findAllCardsByExpiration(UUID userId, CardSortOrder order, Pageable pageable) {
        switch (order) {
            case EARLIEST -> {
                return cardRepository.findByUserIdOrderByExpirationDateEarliest(userId, pageable);
            } case LATEST -> {
                return cardRepository.findByUserIdOrderByExpirationDateLatest(userId, pageable);
            } default -> {
                return List.of();
            }
        }
    }

    @Transactional(readOnly = true)
    public List<Card> findAllCardsByLimit(UUID userId, CardSortOrder order, Pageable pageable) {
        switch (order) {
            case DESC -> {
                return cardRepository.findByUserIdOrderByLimitValueDesc(userId, pageable);
            } case ASC -> {
                return cardRepository.findByUserIdOrderByLimitValueAsc(userId, pageable);
            } default -> {
                return List.of();
            }
        }
    }
}
