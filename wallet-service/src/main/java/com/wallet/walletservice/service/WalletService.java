package com.wallet.walletservice.service;

import com.wallet.walletservice.dto.AddCardDto;
import com.wallet.walletservice.dto.CardPreviewDto;
import com.wallet.walletservice.dto.SaveCardDto;
import com.wallet.walletservice.feign.WalletClient;
import com.wallet.walletservice.mapper.CardDtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WalletService {
    private final WalletClient walletClient;
    private final CardDtoMapper cardDtoMapper;

    @Transactional
    public void saveCard(AddCardDto addCardDto, UUID userId, String email) {
        SaveCardDto saveCardDto = cardDtoMapper.toDto(addCardDto);
        saveCardDto.setUserId(userId);
        saveCardDto.setEmail(email);
        walletClient.saveCard(saveCardDto);
    }

    public List<CardPreviewDto> getLinkedCards(UUID userId) {
        return walletClient.getLinkedCards(userId).getBody();
    }
}
