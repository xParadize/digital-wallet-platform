package com.wallet.walletservice.service;

import com.wallet.walletservice.dto.*;
import com.wallet.walletservice.enums.CardSortOrder;
import com.wallet.walletservice.enums.CardSortType;
import com.wallet.walletservice.feign.CardFeignClient;
import com.wallet.walletservice.feign.TransactionFeignClient;
import com.wallet.walletservice.mapper.CardDtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WalletService {
    private final CardFeignClient cardFeignClient;
    private final CardDtoMapper cardDtoMapper;

    @Transactional
    public void saveCard(AddCardDto addCardDto, UUID userId, String email) {
        SaveCardDto saveCardDto = cardDtoMapper.toDto(addCardDto);
        saveCardDto.setUserId(userId);
        saveCardDto.setEmail(email);
        cardFeignClient.saveCard(saveCardDto);
    }

    public List<CardPreviewDto> getLinkedCards(UUID userId, CardSortType sort, CardSortOrder order) {
        return cardFeignClient.getLinkedCards(userId, sort, order).getBody();
    }

    public CardDetailsDto getCardById(Long cardId, UUID userId) {
        return cardFeignClient.getCardById(cardId, userId).getBody();
    }

    @Transactional
    public void removeCard(String number, UUID userId) {
        cardFeignClient.removeCard(number, userId);
    }
}
