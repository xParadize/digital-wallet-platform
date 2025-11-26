package com.wallet.cardservice.service;

import com.wallet.cardservice.dto.CardInfoDto;
import com.wallet.cardservice.entity.Card;
import com.wallet.cardservice.entity.CardDetails;
import com.wallet.cardservice.entity.CardMetadata;
import com.wallet.cardservice.entity.Limit;
import com.wallet.cardservice.exception.CardNotFoundException;
import com.wallet.cardservice.feign.UserFeignClient;
import com.wallet.cardservice.mapper.*;
import com.wallet.cardservice.repository.CardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CardCacheService {
    private final CardRepository cardRepository;
    private final CardLimitService cardLimitService;
    private final UserFeignClient userFeignClient;
    private final HolderMapper holderMapper;
    private final CardMetadataMapper cardMetadataMapper;
    private final CardDetailsMapper cardDetailsMapper;
    private final CardLimitMapper cardLimitMapper;
    private final CardMapper cardMapper;

    @Cacheable(value = "card", key = "#cardId + ':user:' + #userId")
    @Transactional(readOnly = true)
    public CardInfoDto getCardInfoCached(Long cardId, UUID userId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException("Card not found"));

        CardDetails cardDetails = card.getCardDetails();
        CardMetadata cardMetadata = card.getCardMetadata();
        Limit limit = cardLimitService.getLimitByCard(card);

        return CardInfoDto.builder()
                .cardDto(cardMapper.toDto(card))
                .cardMetadataDto(cardMetadataMapper.toDto(cardMetadata))
                .holder(holderMapper.toEntity(userFeignClient.getCardHolder(userId).getBody()))
                .secretDetails(cardDetailsMapper.toDto(cardDetails))
                .recentTransactions(Collections.emptyList())
                .limit(cardLimitMapper.toDto(limit))
                .build();
    }
}
