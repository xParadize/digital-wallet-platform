package com.wallet.walletservice.service;

import com.wallet.walletservice.dto.*;
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
    private final TransactionFeignClient transactionFeignClient;
    private final CardDtoMapper cardDtoMapper;

    private final int LAST_TRANSACTIONS_LIMIT = 3;

    @Transactional
    public void saveCard(AddCardDto addCardDto, UUID userId, String email) {
        SaveCardDto saveCardDto = cardDtoMapper.toDto(addCardDto);
        saveCardDto.setUserId(userId);
        saveCardDto.setEmail(email);
        cardFeignClient.saveCard(saveCardDto);
    }

    public List<CardPreviewDto> getLinkedCards(UUID userId) {
        return cardFeignClient.getLinkedCards(userId).getBody();
    }

    public CardDetailsDto getLinkedCard(String number, UUID userId) {
        CardDetailsDto cardDetails = cardFeignClient.getLinkedCard(number, userId).getBody();
        List<TransactionDto> recentTransactions = transactionFeignClient.getRecentTransactions(number, LAST_TRANSACTIONS_LIMIT);
        cardDetails.setRecentTransactions(recentTransactions);
        return cardDetails;
    }

    @Transactional
    public void removeCard(String number, UUID userId) {
        cardFeignClient.removeCard(number, userId);
    }
}
