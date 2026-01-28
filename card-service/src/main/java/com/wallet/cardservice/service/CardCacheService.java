package com.wallet.cardservice.service;

import com.wallet.cardservice.dto.CachedCardInfo;
import com.wallet.cardservice.dto.Holder;
import com.wallet.cardservice.entity.Card;
import com.wallet.cardservice.exception.CardNotFoundException;
import com.wallet.cardservice.feign.UserFeignClient;
import com.wallet.cardservice.mapper.CardDetailsMapper;
import com.wallet.cardservice.mapper.CardMapper;
import com.wallet.cardservice.mapper.CardMetadataMapper;
import com.wallet.cardservice.mapper.LimitMapper;
import com.wallet.cardservice.repository.CardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CardCacheService {
    private final CardRepository cardRepository;
    private final UserFeignClient userFeignClient;
    private final CardMetadataMapper cardMetadataMapper;
    private final CardDetailsMapper cardDetailsMapper;
    private final LimitMapper limitMapper;
    private final CardMapper cardMapper;

    @Cacheable(value = "card", key = "#cardId")
    @Transactional(readOnly = true)
    public CachedCardInfo getCardInfoCached(Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException("Card not found"));

        return buildCachedCardInfo(card);
    }

    @Cacheable(value = "holder", key = "#userId")
    public Holder getHolderCached(UUID userId) {
        return userFeignClient.getCardHolder(userId).getBody();
    }

    @CacheEvict(value = "card", key = "#cardId")
    public void evictCardById(Long cardId) {}

    @CacheEvict(value = "cardByNumber", key = "#number")
    public void evictAllCardsByNumber(String number) {}

    private CachedCardInfo buildCachedCardInfo(Card card) {
        return CachedCardInfo.builder()
                .userId(card.getUserId())
                .cardDto(cardMapper.toDto(card))
                .cardMetadataDto(cardMetadataMapper.toDto(card.getCardMetadata()))
                .secretDetails(cardDetailsMapper.toDto(card.getCardDetails()))
                .limit(limitMapper.toDto(card.getLimit()))
                .build();
    }
}
