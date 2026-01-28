package com.wallet.cardservice.service;

import com.wallet.cardservice.dto.CachedCardInfo;
import com.wallet.cardservice.dto.CardInfoDto;
import com.wallet.cardservice.dto.Holder;
import com.wallet.cardservice.entity.Card;
import com.wallet.cardservice.exception.CardAccessDeniedException;
import com.wallet.cardservice.repository.CardRepository;
import com.wallet.cardservice.util.CardSecurityProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CardInfoService {
    private final CardRepository cardRepository;
    private final CardCacheService cardCacheService;

    @Transactional(readOnly = true)
    public CardInfoDto getCardInfoById(Long cardId, UUID userId) {
        CachedCardInfo cachedInfo = cardCacheService.getCardInfoCached(cardId);

        if (!cachedInfo.userId().equals(userId)) {
            throw new CardAccessDeniedException("Access to the card is forbidden");
        }

        Holder holder = cardCacheService.getHolderCached(userId);

        return buildCardInfoDto(cachedInfo, holder);
    }

    @Transactional(readOnly = true)
    public CardInfoDto getCardInfoByNumber(String cardNumber, UUID userId) {
        Card card = cardRepository.findByCardNumberAndUserIdWithDetails(cardNumber, userId)
                .orElseThrow(() -> new CardAccessDeniedException("Access to the card is forbidden"));

        CachedCardInfo cachedInfo = cardCacheService.getCardInfoCached(card.getId());
        Holder holder = cardCacheService.getHolderCached(userId);

        return buildCardInfoDto(cachedInfo, holder);
    }

    private CardInfoDto buildCardInfoDto(CachedCardInfo cachedInfo, Holder holder) {
        return CardInfoDto.builder()
                .cardDto(cachedInfo.cardDto())
                .cardMetadataDto(cachedInfo.cardMetadataDto())
                .secretDetails(cachedInfo.secretDetails())
                .limit(cachedInfo.limit())
                .holder(holder)
                .recentTransactions(Collections.emptyList())
                .build();
    }
}
